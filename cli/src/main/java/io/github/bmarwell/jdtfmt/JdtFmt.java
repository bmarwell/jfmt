package io.github.bmarwell.jdtfmt;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.jansi.graalvm.AnsiConsole;

@CommandLine.Command(
        name = "jdtfmt",
        mixinStandardHelpOptions = true,
        version = "jdtfmt 1.0",
        description = "A command-line tool to format Java source code using JDT."
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
                          Output in diff format. Normal diff is used unless -u is also given."""
    )
    private boolean reportAsDiff;

    @CommandLine.Option(
            names = { "-u", "--unified" },
            description = """
                          Output diff in unified format. Only has effect in conjunction with -d.
                          The optional argument specifies the number of context lines to show.
                          Defaults to ${DEFAULT-VALUE}""",
            arity = "0..1",
            defaultValue = "3",
            fallbackValue = "3"
    )
    private int[] unified;

    @CommandLine.Parameters(
            description = """
                          Files or directory to scan and to format.""",
            arity = "1..*"
    )
    Path[] filesOrDirectories;

    public static void main(String[] args) {
        int exitCode;

        try (AnsiConsole ansi = AnsiConsole.windowsInstall()) {
            final CommandLine cmd = new CommandLine(new JdtFmt());
            final var parseResult = cmd.parseArgs(args);
            exitCode = cmd.execute(args);
        }

        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {

        return 0;
    }
}
