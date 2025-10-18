package io.github.bmarwell.jfmt.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PrintTest extends AbstractCommandTest {

    @Test
    void can_print_with_sorted_imports() {
        // given
        var args = new String[] { "print", "target/test-classes/diff/SomeRecord.java" };
        String importDec = """
                           import static io.github.bmarwell.jdtfmt.format.FormatterMode.DIFF;

                           import io.github.bmarwell.jdtfmt.writer.OutputWriter;

                           import java.lang.String;
                           """;

        // when
        var executionResult = doExecute(args);

        // then
        assertEquals(1, executionResult.returncode());
        assertFalse(executionResult.stdout().getFirst().startsWith("import java.lang.String;"));

        String formattedSource = String.join("\n", executionResult.stdout());
        assertTrue(formattedSource.startsWith(importDec));
    }
}
