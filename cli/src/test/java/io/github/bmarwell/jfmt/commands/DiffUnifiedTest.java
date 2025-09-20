package io.github.bmarwell.jfmt.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DiffUnifiedTest extends AbstractCommandTest {

    @Test
    void can_output_unified_diff() {
        var args = new String[] { "diff", "-u", "target/test-classes/diff/SomeRecord.java" };

        // when
        var executionResult = doExecute(args);

        // then
        assertEquals(1, executionResult.returncode());
        assertTrue(executionResult.stdout().stream().anyMatch(s -> s.startsWith("+")));
    }

}
