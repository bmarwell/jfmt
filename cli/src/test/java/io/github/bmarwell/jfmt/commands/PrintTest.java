package io.github.bmarwell.jfmt.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PrintTest extends AbstractCommandTest {

    @Test
    void can_print_with_sorted_imports() {
        // given
        String importDec = """
                           import static io.github.bmarwell.jdtfmt.format.FormatterMode.DIFF;

                           import io.github.bmarwell.jdtfmt.writer.OutputWriter;
                           import java.lang.String;
                           """;
        var args = new String[] { "print", pathToSomeRecord() };

        // when
        var executionResult = doExecute(args);

        // then
        assertEquals(1, executionResult.returncode(), "returncode should be 1");
        assertFalse(
            executionResult.stdout().getFirst().startsWith("import java.lang.String;"),
            "stdout should not contain java.lang.String"
        );

        String formattedSource = String.join(System.lineSeparator(), executionResult.stdout());
        String stderr = String.join(System.lineSeparator(), executionResult.stderr());
        assertFalse(
            formattedSource.isEmpty(),
            "formattedSource on stdout should not be empty, but is. stderr: " + stderr
        );
        assertTrue(
            formattedSource.startsWith(importDec),
            "stdout should start with import declaration but started with: "
                + formattedSource.substring(0, Math.min(formattedSource.length(), 100))
        );
    }
}
