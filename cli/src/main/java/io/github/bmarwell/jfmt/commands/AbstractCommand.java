package io.github.bmarwell.jfmt.commands;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import io.github.bmarwell.jfmt.config.ConfigLoader;
import io.github.bmarwell.jfmt.config.NamedConfig;
import io.github.bmarwell.jfmt.format.FileProcessingResult;
import io.github.bmarwell.jfmt.format.FormatterMode;
import io.github.bmarwell.jfmt.nio.PathUtils;
import io.github.bmarwell.jfmt.writer.OutputWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
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
            final String revisedSourceCode = createRevisedSourceCode(formatter, javaFile, sourceCode);

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
        } catch (BadLocationException | CoreException ble) {
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

    static String createRevisedSourceCode(CodeFormatter formatter, Path javaFile, String sourceCode)
        throws BadLocationException, CoreException {
        CompilationUnit compilationUnit = getCompilationUnitFrom(sourceCode, javaFile);

        if (compilationUnit.getProblems() != null && compilationUnit.getProblems().length > 0) {
            Stream.of(compilationUnit.getProblems())
                .forEach(System.err::println);
            throw new IllegalStateException(
                "CompilationUnit problens: " + Arrays.asList(compilationUnit.getProblems())
            );
        }

        // cannot use ImportRewrite which requires an Eclipse Workspace
        // Collect imports
        @SuppressWarnings("unchecked")
        List<ImportDeclaration> imports = new ArrayList<>(compilationUnit.imports());

        StringBuilder sb = new StringBuilder();
        List<ImportDeclaration> staticImports = new ArrayList<>();
        List<ImportDeclaration> normalImports = new ArrayList<>();
        List<ImportDeclaration> javaImports = new ArrayList<>();

        for (ImportDeclaration id : imports) {
            String name = id.getName().getFullyQualifiedName();
            if ((Modifier.isStatic(id.getModifiers())))
                staticImports.add(id);
            else if (name.startsWith("java"))
                javaImports.add(id);
            else
                normalImports.add(id);
        }

        Consumer<List<ImportDeclaration>> appendGroup = list -> {
            for (ImportDeclaration id : list) {
                // includes trailing newline
                sb.append(id.toString());
            }

            // extra blank line between groups
            sb.append("\n");
        };

        appendGroup.accept(staticImports);
        appendGroup.accept(normalImports);
        appendGroup.accept(javaImports);

        // Replace imports list manually
        int importStart = compilationUnit.imports().isEmpty()
            ? 0
            : ((ImportDeclaration) compilationUnit.imports().getFirst()).getStartPosition();
        int importEnd = compilationUnit.imports().isEmpty()
            ? 0
            : compilationUnit.imports()
                .stream()
                .mapToInt(id -> ((ASTNode) id).getStartPosition() + ((ASTNode) id).getLength())
                .max()
                .orElse(0);

        final IDocument docImport = new Document(sourceCode);
        docImport.replace(importStart, importEnd - importStart, sb.toString());
        TextEdit importRewrite = compilationUnit.rewrite(docImport, null);
        importRewrite.apply(docImport);

        final TextEdit edit = formatter.format(
            CodeFormatter.K_COMPILATION_UNIT,
            sourceCode,
            0,
            sourceCode.length(),
            0,
            "\n"
        );

        Objects.requireNonNull(edit, "Formatting edits must not be null.");

        edit.apply(docImport);

        return docImport.get();
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

        org.eclipse.jdt.core.dom.CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        compilationUnit.recordModifications();

        // Create a package + CU
        return compilationUnit;
    }

    public OutputWriter getWriter() {
        return writer;
    }
}
