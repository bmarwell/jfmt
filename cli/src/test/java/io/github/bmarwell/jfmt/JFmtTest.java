package io.github.bmarwell.jfmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.jfmt.commands.AbstractCommandTest;
import org.junit.jupiter.api.Test;

class JFmtTest extends AbstractCommandTest {

    @Test
    void shows_help_on_invalid_path() {
        // given
        String[] args = { "doesntexist" };

        // when
        var executionResult = doExecute(args);

        // then
        assertEquals(
            3,
            executionResult.returncode(),
            "should exit with code 2, stderr: " + executionResult.stderr() + "\nstdout: " + executionResult.stdout()
        );
        assertTrue(
            String.join("\n", executionResult.stderr()).contains("doesntexist"),
            "stderr should mention the invalid path:\n" + executionResult.stderr()
        );
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

    @Test
    void lists_file() {
        // given
        String[] args = { "--list", "src/test/resources" };

        // when
        var executionResult = doExecute(args);

        // then
        assertEquals(
            1,
            executionResult.returncode(),
            "should exit with code 1, stderr: " + executionResult.stderr() + "\nstdout: " + executionResult.stdout()
        );
    }
}
