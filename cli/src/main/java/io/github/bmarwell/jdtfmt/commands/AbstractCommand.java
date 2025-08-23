package io.github.bmarwell.jdtfmt.commands;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import io.github.bmarwell.jdtfmt.config.ConfigLoader;
import io.github.bmarwell.jdtfmt.config.NamedConfig;
import io.github.bmarwell.jdtfmt.format.FileProcessingResult;
import io.github.bmarwell.jdtfmt.format.FormatterMode;
import io.github.bmarwell.jdtfmt.nio.PathUtils;
import io.github.bmarwell.jdtfmt.writer.OutputWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import picocli.CommandLine;

public abstract class AbstractCommand implements Callable<Integer> {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Mixin
    GlobalOptions globalOptions = new GlobalOptions();

    private OutputWriter writer;

    public void init() {
        CommandLine.Help.Ansi ansiMode =
                this.globalOptions.noColor ? CommandLine.Help.Ansi.OFF : CommandLine.Help.Ansi.AUTO;
        this.writer = new OutputWriter(
                ansiMode,
                getFormatterMode().verbose(),
                spec.commandLine().getOut(),
                spec.commandLine().getErr()
        );
    }

    abstract FormatterMode getFormatterMode();

    @Override
    public Integer call() throws Exception {
        final Set<Path> allFilesAndDirs = PathUtils.resolveAll(List.of(this.globalOptions.filesOrDirectories));
        final CodeFormatter formatter = createCodeFormatter();
        final ArrayList<FileProcessingResult> results = new ArrayList<>();

        Iterator<Path> iterator = allFilesAndDirs.iterator();
        while (iterator.hasNext()) {
            Path javaFile = iterator.next();
            final FileProcessingResult processingResult = processFile(formatter, javaFile);

            results.add(processingResult);

            // short-circuit if not -e was given
            if (!processingResult.shouldContinue() && iterator.hasNext()) {
                return 1;
            }
        }

        return results.stream().anyMatch(FileProcessingResult::hasDiff) ? 1 : 0;
    }

    FileProcessingResult processFile(CodeFormatter formatter, Path javaFile) {
        getWriter().info("Processing file", javaFile.toString());

        try (var javaSource = Files.newInputStream(javaFile)) {
            final String sourceCode = new String(javaSource.readAllBytes(), StandardCharsets.UTF_8);
            final String revisedSourceCode = createRevisedSourceCode(formatter, sourceCode);

            final List<String> originalSourceLines = List.of(sourceCode.split("\n"));
            final List<String> revisedSourceLines = List.of(revisedSourceCode.split("\n"));
            final Patch<String> patch = DiffUtils.diff(originalSourceLines, revisedSourceLines);

            return processRevisedSourceCode(
                javaFile,
                sourceCode,
                revisedSourceCode,
                originalSourceLines,
                revisedSourceLines,
                patch
            );
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        } catch (org.eclipse.jface.text.BadLocationException ble) {
            getWriter().warn("Error formatting file", javaFile.toString());
            throw new IllegalStateException(ble);
        }
    }

    abstract FileProcessingResult processRevisedSourceCode(
            Path javaFile,
            String sourceCode,
            String revisedSourceCode,
            List<String> originalSourceLines,
            List<String> revisedSourceLines,
            Patch<String> patch
    );

    private CodeFormatter createCodeFormatter() {
        if (this.globalOptions.configFile != null && Files.isRegularFile(this.globalOptions.configFile)) {
            final Map<String, String> config = ConfigLoader.load(this.globalOptions.configFile);

            return ToolFactory.createCodeFormatter(config);
        }

        final NamedConfig nc = NamedConfig.valueOf(this.globalOptions.config.name());
        final Map<String, String> config = ConfigLoader.load(nc.getResourcePath());

        return ToolFactory.createCodeFormatter(config);
    }

    static String createRevisedSourceCode(CodeFormatter formatter, String sourceCode)
            throws BadLocationException {
        final TextEdit edit = formatter.format(
            CodeFormatter.K_COMPILATION_UNIT,
            sourceCode,
            0,
            sourceCode.length(),
            0,
            "\n"
        );

        Objects.requireNonNull(edit, "Formatting edits must not be null.");

        final IDocument doc = new Document(sourceCode);
        edit.apply(doc);

        return doc.get();
    }

    public OutputWriter getWriter() {
        return writer;
    }
}
