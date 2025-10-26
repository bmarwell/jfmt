package io.github.bmarwell.jfmt;

import io.github.bmarwell.jfmt.commands.AbstractCommand;
import io.github.bmarwell.jfmt.commands.Diff;
import io.github.bmarwell.jfmt.commands.List;
import io.github.bmarwell.jfmt.commands.Print;
import io.github.bmarwell.jfmt.commands.Write;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.jansi.graalvm.AnsiConsole;

@CommandLine.Command(
    name = "jfmt",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "A command-line tool to format Java source code using JDT.",
    footer = {
        "",
        "If no subcommand is specified, 'write' is used as the default.",
        "Run 'jfmt write --help' to see all available options and parameters."
    },
    usageHelpAutoWidth = true,
    subcommands = {
        List.class,
        Write.class,
        Print.class,
        Diff.class,
    }
)
public class JFmt implements Callable<Integer> {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    public static void main(String[] args) {
        int exitCode;

        try (AnsiConsole ansi = AnsiConsole.windowsInstall()) {
            final CommandLine cmd = new CommandLine(new JFmt());
            cmd.setUnmatchedArgumentsAllowed(true);
            cmd.setExecutionStrategy(new WriteDefaultExecutionStrategy());
            exitCode = cmd.execute(args);
        }

        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        // JFmt should not be called directly - subcommands should be used
        spec.commandLine().usage(spec.commandLine().getOut());
        return CommandLine.ExitCode.USAGE;
    }

    private static class WriteDefaultExecutionStrategy implements CommandLine.IExecutionStrategy {
        @Override
        public int execute(CommandLine.ParseResult parseResult) throws CommandLine.ExecutionException {
            // If no subcommand was specified but there are unmatched arguments,
            // assume they meant 'write' as the default command
            // Note: This means "jfmt --help" won't show <filesOrDirectories> parameters,
            // since those belong to the Write subcommand, not the parent JFmt command.
            // Trade-off: We use the footer in @Command to help users discover that 'write' is the default subcommand,
            // because picocli does not natively support showing subcommand parameters in the parent help output.
            // This is a UX limitation: users may not immediately see which parameters are accepted by default.

            // TODO: If picocli adds support for showing default subcommand parameters in the parent help,
            // or if we can find a workaround, update the help output to make this clearer.
            //
            // TODO: Future enhancement - when stdin support is added:
            // - If input comes from stdin (no files specified), default to 'print' instead of 'write'
            // - This allows: cat MyFile.java | jfmt | less
            if (parseResult.subcommand() == null && !parseResult.unmatched().isEmpty()) {
                // Build new args with 'write' prepended
                String[] originalArgs = parseResult.originalArgs().toArray(new String[0]);
                String[] newArgs = new String[originalArgs.length + 1];
                newArgs[0] = "write";
                System.arraycopy(originalArgs, 0, newArgs, 1, originalArgs.length);

                // Reparse and execute with 'write' command
                return parseResult.commandSpec().commandLine().execute(newArgs);
            }

            // Initialize AbstractCommand if a subcommand was used
            CommandLine.ParseResult commandResult = parseResult;
            while (commandResult.hasSubcommand()) {
                commandResult = commandResult.subcommand();
            }

            if (commandResult.commandSpec().userObject() instanceof AbstractCommand abstractCommand) {
                abstractCommand.init();
            }

            return new CommandLine.RunLast().execute(parseResult);
        }
    }

}
