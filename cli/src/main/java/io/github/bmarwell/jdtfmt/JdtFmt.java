package io.github.bmarwell.jdtfmt;

import io.github.bmarwell.jdtfmt.config.CliNamedConfig;
import io.github.bmarwell.jdtfmt.config.ConfigLoader;
import io.github.bmarwell.jdtfmt.config.NamedConfig;
import io.github.bmarwell.jdtfmt.format.DiffOptions;
import io.github.bmarwell.jdtfmt.format.FileProcessingResult;
import io.github.bmarwell.jdtfmt.format.FormatProcessor;
import io.github.bmarwell.jdtfmt.format.FormatterMode;
import io.github.bmarwell.jdtfmt.nio.PathUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private FormatterMode fmtMode;
    private FormatProcessor formatProcessor;

    public static void main(String[] args) {
        int exitCode;

        try (AnsiConsole ansi = AnsiConsole.windowsInstall()) {
            final JdtFmt jdtFmt = new JdtFmt();
            final CommandLine cmd = new CommandLine(jdtFmt);

            try {
                final var parseResult = cmd.parseArgs(args);
                jdtFmt.init();

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

    public void init() {
        this.ansiMode = this.noColor ? CommandLine.Help.Ansi.OFF : CommandLine.Help.Ansi.AUTO;
        this.fmtMode = getFormatterMode();
        this.formatProcessor = new FormatProcessor(this.fmtMode, new DiffOptions(this.unified[0]));
    }

    private FormatterMode getFormatterMode() {
        if (this.write) {
            // write implies list
            return FormatterMode.WRITE;
        }

        if (this.listOnly) {
            if (this.reportAll) {
                return FormatterMode.LIST_ALL;
            } else {
                return FormatterMode.LIST_FIRST;
            }
        }

        if (this.reportAsDiff) {
            if (this.unified[0] >= 0) {
                if (this.reportAll) {
                    return FormatterMode.DIFF_ALL_UNIFIED;
                } else {
                    return FormatterMode.DIFF_FIRST_UNIFIED;
                }
            } else {
                if (this.reportAll) {
                    return FormatterMode.DIFF_ALL_NORMAL;
                } else {
                    return FormatterMode.DIFF_FIRST_NORMAL;
                }
            }
        }

        return FormatterMode.PRINT;
    }

    @Override
    public Integer call() {
        final Set<Path> allFilesAndDirs = PathUtils.resolveAll(List.of(this.filesOrDirectories));
        final CodeFormatter formatter = createCodeFormatter();
        final ArrayList<FileProcessingResult> results = new ArrayList<>();

        for (Path javaFile : allFilesAndDirs) {
            final FileProcessingResult processingResult = processFile(formatter, javaFile);

            results.add(processingResult);

            if (processingResult.changesWritten()) {
                System.err.println(ansiMode.string("@|bold,green Wrote formatted file:|@ " + javaFile));
            } else if (processingResult.hasDiff()) {
                System.err.println(ansiMode.string("@|bold,red Not formatted correctly:|@ " + javaFile));
            }

            // short-circuit if not -e was given
            if (processingResult.hasDiff() && !this.reportAll && !this.write) {
                return 1;
            }
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
        System.err.println(ansiMode.string("@|bold,green Processing file:|@ " + javaFile));

        try (var javaSource = Files.newInputStream(javaFile)) {
            final String sourceCode = new String(javaSource.readAllBytes(), StandardCharsets.UTF_8);
            final String revisedSourceCode = createRevisedSourceCode(formatter, sourceCode);

            return formatProcessor.processRevisedSourceCode(javaFile, sourceCode, revisedSourceCode);
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

}
