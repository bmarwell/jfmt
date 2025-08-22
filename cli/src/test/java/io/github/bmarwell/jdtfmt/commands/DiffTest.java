package io.github.bmarwell.jdtfmt.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.jdtfmt.JdtFmt;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class DiffTest {

    @Test
    void can_output_unified_diff() {
        var args = new String[] { "diff", "-u", "target/test-classes/diff/SomeRecord.java" };

        JdtFmt jdtFmt = new JdtFmt();
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

        // then
        assertEquals(1, execute);
        assertTrue(out.toString().contains("+"));
    }
}
