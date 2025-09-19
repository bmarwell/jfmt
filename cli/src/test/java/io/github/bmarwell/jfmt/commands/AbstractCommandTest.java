package io.github.bmarwell.jfmt.commands;

import io.github.bmarwell.jfmt.JFmt;
import io.github.bmarwell.jfmt.test.CommandExecutionResult;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import picocli.CommandLine;

public abstract class AbstractCommandTest {

    protected CommandExecutionResult doExecute(String[] args) throws CommandLine.UnmatchedArgumentException {
        JFmt jdtFmt = new JFmt();
        CommandLine cmd = new CommandLine(jdtFmt);

        var out = new StringWriter();
        var err = new StringWriter();
        cmd.setOut(new PrintWriter(out));
        cmd.setErr(new PrintWriter(err));

        CommandLine.ParseResult parseResult = cmd.parseArgs(args);
        CommandLine.ParseResult commandParseResult = parseResult;
        while (commandParseResult.hasSubcommand()) {
            commandParseResult = commandParseResult.subcommand();
        }

        if (commandParseResult.commandSpec().userObject() instanceof AbstractCommand abstractCommand) {
            abstractCommand.init();
        }

        // when
        int execute = cmd.execute(args);

        return new CommandExecutionResult(
            execute,
            List.of(out.toString().split("\n")),
            List.of(err.toString().split("\n"))
        );
    }
}
