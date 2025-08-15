package io.github.bmarwell.jdtfmt;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import io.github.bmarwell.jdtfmt.config.CliNamedConfig;
import io.github.bmarwell.jdtfmt.config.ConfigLoader;
import io.github.bmarwell.jdtfmt.config.NamedConfig;
import io.github.bmarwell.jdtfmt.nio.PathUtils;
import io.github.bmarwell.jdtfmt.value.FileProcessingResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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
import picocli.jansi.graalvm.AnsiConsole;

@CommandLine.Command(
        name = "jdtfmt",
        mixinStandardHelpOptions = true,
        version = "jdtfmt 1.0",
        description = "A command-line tool to format Java source code using JDT.",
        usageHelpAutoWidth = true
)
public class JdtFmt implements Callable<Integer> {

    @CommandLine.Option(
            names = { "-w", "--write" },
            description = """
                          Write the formatted source code back to the file(s).
                          If not given, the non-indented file(s) will be printed to stdout.
                          Only the first file is printed, unless -e is also given.""",
            defaultValue = "false"
    )
    private boolean write;

    @CommandLine.Option(
            names = { "-e" },
            description = """
                          Report all errors, not just the first one."""
    )
    private boolean reportAll;

    @CommandLine.Option(
            names = { "-l", "--list" },
            description = """
                          Just report the name of the files which are not indented correctly."""
    )
    private boolean listOnly;

    @CommandLine.Option(
            names = { "-d", "--diff" },
            description = """
                          Output in diff format. Normal diff is used unless -u is also given.
                          Implicitly ignores -w (does never overwrite files)."""
    )
    private boolean reportAsDiff;

    @CommandLine.Option(
            names = { "-u", "--unified" },
            description = """
                          Output diff in unified format. Only has effect in conjunction with -d.
                          The optional argument specifies the number of context lines to show.
                          Deactivated by default, the default value (if no value is given) defaults to 3.""",
            arity = "0..1",
            defaultValue = "-1",
            fallbackValue = "3"
    )
    private int[] unified;

    @CommandLine.Parameters(
            description = """
                          Files or directory to scan and to format.""",
            arity = "1..*"
    )
    Path[] filesOrDirectories;

    @CommandLine.Option(
            names = { "--no-colour", "--no-color" },
            description = "Force no colored output, even if the terminal supports it."
    )
    private boolean noColor;

    @CommandLine.Option(
            names = { "--config" },
            description = """
                          Named config. Default: ${DEFAULT-VALUE}.
                          Available configs: ${COMPLETION-CANDIDATES}""",
            defaultValue = "builtin"
    )
    private CliNamedConfig config = CliNamedConfig.builtin;

    @CommandLine.Option(
            names = { "--config-file" },
            description = """
                          Path to a config file. If unset (default), the named config (--config) will be used."""
    )
    private Path configFile;

    private CommandLine.Help.Ansi ansiMode;

    public void initAnsiMode() {
        ansiMode = noColor ? CommandLine.Help.Ansi.OFF : CommandLine.Help.Ansi.AUTO;
    }

    public static void main(String[] args) {
        int exitCode;

        try (AnsiConsole ansi = AnsiConsole.windowsInstall()) {
            final JdtFmt jdtFmt = new JdtFmt();
            final CommandLine cmd = new CommandLine(jdtFmt);

            try {
                final var parseResult = cmd.parseArgs(args);
                jdtFmt.initAnsiMode();

                exitCode = cmd.execute(args);

                System.exit(exitCode);
            } catch (CommandLine.MissingParameterException mpe) {
                System.err.println(mpe.getMessage());
                System.err.println();
                cmd.usage(System.err);
                System.exit(2);
            }
        }
    }

    @Override
    public Integer call() {
        final Set<Path> allFilesAndDirs = PathUtils.resolveAll(List.of(this.filesOrDirectories));
        final CodeFormatter formatter = createCodeFormatter();
        final ArrayList<FileProcessingResult> results = new ArrayList<>();

        for (Path javaFile : allFilesAndDirs) {
            final FileProcessingResult processingResult = processFile(formatter, javaFile);

            if (processingResult.hasDiff() && !this.reportAll) {
                return 1;
            }

            results.add(processingResult);
        }

        return results.stream().anyMatch(FileProcessingResult::hasDiff) ? 1 : 0;
    }

    private CodeFormatter createCodeFormatter() {
        if (this.configFile != null && Files.isRegularFile(this.configFile)) {
            final Map<String, String> config = ConfigLoader.load(this.configFile);

            return ToolFactory.createCodeFormatter(config);
        }

        final NamedConfig nc = NamedConfig.valueOf(this.config.name());
        final Map<String, String> config = ConfigLoader.load(nc.getResourcePath());

        return ToolFactory.createCodeFormatter(config);
    }

    public FileProcessingResult processFile(CodeFormatter formatter, Path javaFile) {
        System.err.println(ansiMode.string("@|bold,green Formatting file:|@ " + javaFile));

        try (var javaSource = Files.newInputStream(javaFile)) {
            final String sourceCode = new String(javaSource.readAllBytes(), StandardCharsets.UTF_8);
            final String revisedSourceCode = createRevisedSourceCode(formatter, sourceCode);

            return processRevisedSourceCode(javaFile, sourceCode, revisedSourceCode);
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        } catch (org.eclipse.jface.text.BadLocationException ble) {
            System.err.println(ansiMode.string("@|red Error formatting file:|@ " + javaFile));
            throw new IllegalStateException(ble);
        }

    }

    private static String createRevisedSourceCode(CodeFormatter formatter, String sourceCode)
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

    private FileProcessingResult processRevisedSourceCode(Path javaFile, String sourceCode, String revisedSourceCode) {
        final List<String> originalSourceLines = List.of(sourceCode.split("\n"));
        final List<String> revisedSourceLines = List.of(revisedSourceCode.split("\n"));
        final Patch<String> patch = DiffUtils.diff(originalSourceLines, revisedSourceLines);

        if (patch.getDeltas().isEmpty()) {
            return new FileProcessingResult(javaFile, false);
        } else if (!patch.getDeltas().isEmpty()) {
            System.err.println(ansiMode.string("@|bold,red Not indented correctly:|@ " + javaFile));
        }

        if (this.listOnly) {
            return new FileProcessingResult(javaFile, true);
        }

        if (this.reportAsDiff && !sourceCode.equals(revisedSourceCode)) {
            // print diff
            if (this.unified.length > 0 && this.unified[0] >= 0) {
                final List<String> theDiff = UnifiedDiffUtils.generateUnifiedDiff(
                    javaFile.toString(),
                    javaFile + ".new",
                    originalSourceLines,
                    patch,
                    this.unified[0]
                );

                for (String line : theDiff) {
                    System.out.println(ansiMode.string(line));
                }

            } else {
                // print normal diff
                for (AbstractDelta<String> delta : patch.getDeltas()) {
                    if (Objects.requireNonNull(delta.getType()) == DeltaType.EQUAL) {
                        continue;
                    }

                    for (String line : delta.getSource().getLines()) {
                        System.out.println(ansiMode.string("<" + line));
                    }

                    for (String line : delta.getTarget().getLines()) {
                        System.out.println(ansiMode.string(">" + line));
                    }
                }
            }
        } else if (this.write) {
            System.err.println(ansiMode.string("@|bold,green Writing formatted file:|@ " + javaFile));
            try (var os =
                    Files.newOutputStream(javaFile, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                os.write(revisedSourceCode.getBytes(StandardCharsets.UTF_8));

                // it does not have a diff ANYMORE, so return false.
                // this will make the app exit without an error code (exit code 0).
                return new FileProcessingResult(javaFile, false);
            } catch (IOException ioException) {
                throw new UncheckedIOException(ioException);
            }
        } else {
            // output formatted source code
            for (String line : revisedSourceLines) {
                System.out.println(line);
            }
        }

        return new FileProcessingResult(javaFile, true);
    }
}
