package io.github.bmarwell.jfmt.commands;

import io.github.bmarwell.jfmt.JFmt;
import io.github.bmarwell.jfmt.test.CommandExecutionResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import picocli.CommandLine;

public abstract class AbstractCommandTest {

    protected static String pathToSomeRecord() {
        return Path.of("target", "test-classes", "diff", "SomeRecord.java").toString();
    }

    protected CommandExecutionResult doExecute(String[] args) throws CommandLine.UnmatchedArgumentException {
        JFmt jdtFmt = new JFmt();
        CommandLine cmd = new CommandLine(jdtFmt);

        var out = new StringWriter();
        var err = new StringWriter();
        cmd.setOut(new PrintWriter(out));
        cmd.setErr(new PrintWriter(err));

        // Parse to get the subcommand and initialize it
        CommandLine.ParseResult commandParseResult = cmd.parseArgs(args);
        while (commandParseResult.hasSubcommand()) {
            commandParseResult = commandParseResult.subcommand();
        }

        if (commandParseResult.commandSpec().userObject() instanceof AbstractCommand abstractCommand) {
            abstractCommand.init();
        }

        // when
        // Execute using the parsed result, not reparsing
        int exitCode = new CommandLine.RunLast().execute(commandParseResult);

        return new CommandExecutionResult(
            exitCode,
            List.of(out.toString().split(System.lineSeparator())),
            List.of(err.toString().split(System.lineSeparator()))
        );
    }
}
