package io.github.bmarwell.jfmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.bmarwell.jfmt.commands.AbstractCommandTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class JFmtTest extends AbstractCommandTest {

    @Test
    void shows_help_on_invalid_command() {
        // given
        String[] args = { "doesntexist" };

        // expect
        Assertions.assertThrows(CommandLine.UnmatchedArgumentException.class, () -> doExecute(args));
    }

    /**
     * This does not actually test the jfmt class.
     */
    @Test
    void shows_help_on_help() {
        // given
        String[] args = { "--help" };

        // when
        var executionResult = doExecute(args);

        // then
        assertEquals(0, executionResult.returncode());
    }
}
