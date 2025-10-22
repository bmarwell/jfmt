package io.github.bmarwell.jfmt.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DiffNormalTest extends AbstractCommandTest {

    @Test
    void can_output_unified_diff() {
        var args = new String[] { "diff", pathToSomeRecord() };

        // when
        var executionResult = doExecute(args);

        // then
        var stderr = String.join(System.lineSeparator(), executionResult.stderr());
        assertEquals(1, executionResult.returncode(), "returncode should be 1 but was not. stderr: " + stderr);
        assertTrue(
            executionResult.stdout().stream().anyMatch(s -> s.startsWith(">")),
            "stdout should contain > but did not. stderr: " + stderr
        );
        assertTrue(executionResult.stdout().stream().anyMatch(s -> s.startsWith("<")), "stdout should contain <");
    }

}
