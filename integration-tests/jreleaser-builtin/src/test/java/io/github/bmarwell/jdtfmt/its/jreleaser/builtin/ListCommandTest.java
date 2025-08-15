package io.github.bmarwell.jdtfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.jdtfmt.its.extension.JdtFmtTest;
import io.github.bmarwell.jdtfmt.its.extension.JdtResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@JdtFmtTest(args = "-l")
class ListCommandTest {

    @Test
    @JdtFmtTest(args = "-l")
    @DisplayName("runs list command without path should exit 2 and print error message")
    void runs_list_command_without_path(JdtResult result) {
        assertEquals(2, result.exitCode());
        assertTrue(result.getStderr().startsWith("Missing required parameter: '"));
    }

    @Test
    @JdtFmtTest(args = { "-l", "src/test/resources/correctly_formatted" })
    @DisplayName("runs list command with path should exit 0 and print file path")
    void runs_list_command_with_path(JdtResult result) {
        assertEquals(0, result.exitCode());
        assertTrue(result.getStderr().contains("src/test/resources/correctly_formatted/CorrectlyFormatted.java"));
    }
}
