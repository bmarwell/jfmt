package io.github.bmarwell.jfmt.commands;

import static java.nio.file.Files.isRegularFile;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import io.github.bmarwell.jfmt.concurrency.FailFastFileProcessingResultJoiner;
import io.github.bmarwell.jfmt.config.ConfigLoader;
import io.github.bmarwell.jfmt.config.NamedConfig;
import io.github.bmarwell.jfmt.format.FileProcessingResult;
import io.github.bmarwell.jfmt.format.FormatterMode;
import io.github.bmarwell.jfmt.imports.CliNamedImportOrder;
import io.github.bmarwell.jfmt.imports.ImportOrderConfiguration;
import io.github.bmarwell.jfmt.imports.ImportOrderLoader;
import io.github.bmarwell.jfmt.imports.NamedImportOrder;
import io.github.bmarwell.jfmt.nio.PathUtils;
import io.github.bmarwell.jfmt.writer.OutputWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Stream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
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
        final Stream<Path> allFilesAndDirs = PathUtils.streamAll(List.of(this.globalOptions.filesOrDirectories));

        try (var scope = StructuredTaskScope.open(new FailFastFileProcessingResultJoiner())) {
            allFilesAndDirs
                .map(javaFile -> (Callable<FileProcessingResult>) () -> processFile(javaFile))
                .forEach(scope::fork);

            final List<StructuredTaskScope.Subtask<FileProcessingResult>> joins = scope.join().toList();

            // Report any failed subtasks
            joins.stream()
                .filter(subtask -> subtask.state() == StructuredTaskScope.Subtask.State.FAILED)
                .forEach(subtask -> {
                    Throwable exception = subtask.exception();
                    String message = exception.getMessage() != null
                        ? exception.getMessage()
                        : exception.getClass().getSimpleName();
                    getWriter().warn("Error processing file", message);
                });

            // Print output sequentially after all parallel processing is complete
            joins.stream()
                .filter(subtask -> subtask.state() == StructuredTaskScope.Subtask.State.SUCCESS)
                .map(StructuredTaskScope.Subtask::get)
                .forEach(result -> result.outputLines().forEach(getWriter()::output));

            // Check if we have any failures
            boolean hasFailures = joins.stream()
                .anyMatch(subtask -> subtask.state() == StructuredTaskScope.Subtask.State.FAILED);

            // Return error code if there were failures
            if (hasFailures) {
                return 1;
            }

            return joins.stream()
                .filter(subtask -> subtask.state() == StructuredTaskScope.Subtask.State.SUCCESS)
                .map(StructuredTaskScope.Subtask::get)
                .anyMatch(FileProcessingResult::hasDiff) ? 1 : 0;
        }
    }

    FileProcessingResult processFile(Path javaFile) {
        getWriter().info("Processing file", javaFile.toString());

        try {
            final var javaSourceBytes = Files.readAllBytes(javaFile);
            final var sourceCode = getEncodedSourceCode(javaSourceBytes);
            final var formatter = createCodeFormatter();
            final var revisedSourceCode = createRevisedSourceCode(formatter, javaFile, sourceCode);

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
            throw new UncheckedIOException("Failed to process file: " + javaFile, ioException);
        } catch (BadLocationException | CoreException ble) {
            getWriter().warn("Error formatting file", javaFile.toString());
            throw new IllegalStateException("Failed to format file: " + javaFile, ble);
        }
    }

    private static String getEncodedSourceCode(byte[] bytes) {
        try {
            // Simple UTF-8 validity check
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
        } catch (CharacterCodingException e) {
            // Otherwise, assume ISO-8859-1 or CP1252 (safe Latin fallbacks)
            return new String(bytes, StandardCharsets.ISO_8859_1);
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

    protected CodeFormatter createCodeFormatter() {
        if (this.globalOptions.configFile != null && Files.isRegularFile(this.globalOptions.configFile)) {
            final Map<String, String> config = ConfigLoader.load(this.globalOptions.configFile);

            return ToolFactory.createCodeFormatter(config);
        }

        final NamedConfig nc = NamedConfig.valueOf(this.globalOptions.config.name());
        final Map<String, String> config = ConfigLoader.load(nc.getResourcePath());

        return ToolFactory.createCodeFormatter(config);
    }

    String createRevisedSourceCode(CodeFormatter formatter, Path javaFile, String sourceCode)
        throws BadLocationException, CoreException {
        var unixSourceCode = sourceCode.replace("\r\n", "\n");
        CompilationUnit compilationUnit = getCompilationUnitFrom(unixSourceCode, javaFile);

        if (compilationUnit.getProblems() != null && compilationUnit.getProblems().length > 0) {
            for (IProblem problem : compilationUnit.getProblems()) {
                getWriter().warn(javaFile.toString(), problem.toString());
            }

            throw new IllegalStateException(
                "CompilationUnit problems: " + Arrays.asList(compilationUnit.getProblems())
            );
        }

        // If there are imports, reorder them deterministically, according to style.
        final IDocument workingDoc = new Document(unixSourceCode);

        ImportOrderProcessor importOrderProcessor = createImportOrderProcessor();
        importOrderProcessor.rewriteImportsIfAny(compilationUnit, workingDoc);

        // Now format the (possibly) updated document
        FormatterProcessor formatterProcessor = new FormatterProcessor(formatter);
        formatterProcessor.formatDocument(workingDoc);

        return workingDoc.get();
    }

    private ImportOrderProcessor createImportOrderProcessor() {
        // Resolve import-order tokens from CLI options
        if (this.globalOptions.importOrderFile != null && isRegularFile(this.globalOptions.importOrderFile)) {
            var importOrderConfig = new ImportOrderLoader().loadFromFile(this.globalOptions.importOrderFile);

            return new ImportOrderProcessor(importOrderConfig);

        }
        CliNamedImportOrder cli = this.globalOptions.importOrder;
        var named = NamedImportOrder.fromCli(cli);
        ImportOrderConfiguration importOrderTokens = new ImportOrderLoader().loadFromResource(named.getResourcePath());

        return new ImportOrderProcessor(importOrderTokens);
    }

    private static CompilationUnit getCompilationUnitFrom(String sourceCode, Path javaFile) {
        // extract package from file
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(sourceCode.toCharArray());
        parser.setUnitName(javaFile.getFileName().toString());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        // Configure compiler options
        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_SOURCE, String.valueOf(AST.getJLSLatest()));
        options.put(JavaCore.COMPILER_COMPLIANCE, String.valueOf(AST.getJLSLatest()));
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, String.valueOf(AST.getJLSLatest()));
        parser.setCompilerOptions(options);

        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        compilationUnit.recordModifications();

        // Create a package + CU
        return compilationUnit;
    }

    public OutputWriter getWriter() {
        return writer;
    }

}
