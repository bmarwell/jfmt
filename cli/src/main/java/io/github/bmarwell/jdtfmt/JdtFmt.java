package io.github.bmarwell.jdtfmt;

import io.github.bmarwell.jdtfmt.commands.AbstractCommand;
import io.github.bmarwell.jdtfmt.commands.Diff;
import io.github.bmarwell.jdtfmt.commands.List;
import io.github.bmarwell.jdtfmt.commands.Print;
import io.github.bmarwell.jdtfmt.commands.Write;
import picocli.CommandLine;
import picocli.jansi.graalvm.AnsiConsole;

@CommandLine.Command(
        name = "jdtfmt",
        mixinStandardHelpOptions = true,
        version = "jdtfmt 1.0",
        description = "A command-line tool to format Java source code using JDT.",
        usageHelpAutoWidth = true,
        subcommands = {
                List.class,
                Write.class,
                Print.class,
                Diff.class,
        }
)
public class JdtFmt {

    @CommandLine.Option(
            names = { "-l", "--list" },
            description = """
                          Just report the name of the files which are not indented correctly."""
    )
    private boolean listOnly;

    public static void main(String[] args) {
        int exitCode;

        try (AnsiConsole ansi = AnsiConsole.windowsInstall()) {
            final JdtFmt jdtFmt = new JdtFmt();
            final CommandLine cmd = new CommandLine(jdtFmt);

            try {
                final var parseResult = cmd.parseArgs(args);

                CommandLine.ParseResult command = parseResult;
                while (command.hasSubcommand()) {
                    command = command.subcommand();
                }

                if (command.isUsageHelpRequested()) {
                    command.commandSpec().commandLine().usage(cmd.getErr());
                    System.exit(0);
                }

                if (!command.errors().isEmpty()) {
                    for (Exception error : command.errors()) {
                        cmd.getErr().println(error.getMessage());
                    }

                    command.commandSpec().commandLine().usage(cmd.getErr());

                    System.exit(2);
                }

                if (command.commandSpec().userObject() instanceof AbstractCommand abstractCommand) {
                    abstractCommand.init();
                }

                exitCode = cmd.execute(args);

                System.exit(exitCode);
            } catch (CommandLine.PicocliException mpe) {
                cmd.getErr().println(mpe.getMessage());
                cmd.getErr().println();
                cmd.usage(cmd.getErr());
                System.exit(2);
            }
        }
    }

}
