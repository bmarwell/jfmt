package io.github.bmarwell.jfmt;

import io.github.bmarwell.jfmt.commands.AbstractCommand;
import io.github.bmarwell.jfmt.commands.Diff;
import io.github.bmarwell.jfmt.commands.DiffOptions;
import io.github.bmarwell.jfmt.commands.GlobalOptions;
import io.github.bmarwell.jfmt.commands.List;
import io.github.bmarwell.jfmt.commands.Print;
import io.github.bmarwell.jfmt.commands.Write;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import picocli.CommandLine;
import picocli.jansi.graalvm.AnsiConsole;

@CommandLine.Command(
    name = "jfmt",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "A command-line tool to format Java source code using JDT.",
    usageHelpAutoWidth = true
)
public class JFmt implements Callable<Integer> {

    @CommandLine.ArgGroup(
        exclusive = true,
        heading = """
                  Mode of operations:
                  If none of these is given, jfmt defaults to 'write' mode and fixes all style issues in place.
                  """,
        multiplicity = "0..1",
        headingKey = "operationModeHeading"
    )
    JfmtOperationModeArgs operationModeArgs = new JfmtOperationModeArgs();

    @CommandLine.Mixin(
        name = "diff options"
    )
    DiffOptions diffOptions = new DiffOptions();

    @CommandLine.Mixin
    GlobalOptions globalOptions = new GlobalOptions();

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    static void main(String[] args) {
        int exitCode;

        try (AnsiConsole _ = AnsiConsole.windowsInstall()) {
            final JFmt jdtFmt = new JFmt();
            final CommandLine cmd = new CommandLine(jdtFmt);

            try {
                final var parseResult = cmd.parseArgs(args);

                if (parseResult.isUsageHelpRequested()) {
                    parseResult.commandSpec().commandLine().usage(cmd.getErr());
                    System.exit(0);
                }

                if (!parseResult.errors().isEmpty()) {
                    for (Exception error : parseResult.errors()) {
                        cmd.getErr().println(error.getMessage());
                    }

                    parseResult.commandSpec().commandLine().usage(cmd.getErr());

                    System.exit(2);
                }

                int rc = cmd.execute(args);
                System.exit(rc);
            } catch (CommandLine.PicocliException mpe) {
                cmd.getErr().println(mpe.getMessage());
                cmd.getErr().println();
                cmd.usage(cmd.getErr());
                System.exit(2);
            }
        }
    }

    @Override
    public Integer call() throws Exception {
        try {
            String[] filteredArgs = filterOutModeArgs(
                spec.commandLine().getParseResult().originalArgs()
            );
            CommandLine actualCommand = getCommandLine(filteredArgs);

            if (actualCommand.getCommandSpec().userObject() instanceof AbstractCommand abstractCommand) {
                abstractCommand.init();
            }

            return actualCommand.execute(filteredArgs);
        } catch (CommandLine.PicocliException mpe) {
            spec.commandLine().getErr().println(mpe.getMessage());
            spec.commandLine().getErr().println();
            spec.commandLine().usage(spec.commandLine().getErr());
            return 2;
        }
    }

    private CommandLine getCommandLine(String[] filteredArgs) {
        CommandLine actualCommand;
        if (this.operationModeArgs != null && this.operationModeArgs.diff) {
            Diff diff = new Diff();
            actualCommand = new CommandLine(diff);
        } else if (this.operationModeArgs != null && this.operationModeArgs.listOnly) {
            List list = new List();
            actualCommand = new CommandLine(list);
        } else if (this.operationModeArgs != null && this.operationModeArgs.stdout) {
            Print print = new Print();
            actualCommand = new CommandLine(print);
        } else {
            Write write = new Write();
            actualCommand = new CommandLine(write);
        }

        actualCommand.setErr(this.spec.commandLine().getErr());
        actualCommand.setOut(this.spec.commandLine().getOut());
        actualCommand.parseArgs(filteredArgs);

        return actualCommand;
    }

    private String[] filterOutModeArgs(java.util.List<String> args) {
        if (args == null || args.isEmpty()) {
            return new String[] {};
        }

        // Known mode flags to remove before parsing the selected subcommand.
        // Adjust these names if your option names differ.
        Class<JfmtOperationModeArgs> aClass = JfmtOperationModeArgs.class;
        Field[] declaredFields = aClass.getDeclaredFields();
        Set<String> modeFlags = Arrays.stream(declaredFields)
            .filter(field -> field.isAnnotationPresent(CommandLine.Option.class))
            .map(field -> field.getAnnotation(CommandLine.Option.class))
            .flatMap(annotationType -> Arrays.stream(annotationType.names()))
            .collect(Collectors.toUnmodifiableSet());

        java.util.List<String> kept = new ArrayList<>(args.size());
        for (String token : args) {
            if (modeFlags.contains(token)) {
                continue;
            }

            kept.add(token);
        }

        return kept.toArray(new String[0]);
    }
}
