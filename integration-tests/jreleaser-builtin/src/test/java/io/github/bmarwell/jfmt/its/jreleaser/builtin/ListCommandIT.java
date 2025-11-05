package io.github.bmarwell.jfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.jfmt.its.extension.JFmtTest;
import io.github.bmarwell.jfmt.its.extension.JdtResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@JFmtTest
class ListCommandIT {

    @Test
    @JFmtTest(args = "list")
    @DisplayName("runs list command without path should exit 2 and print error message")
    void runs_list_command_without_path(JdtResult result) {
        assertEquals(2, result.exitCode(), () -> "Wrong exit code, stderr:" + result.getStderr());
        assertTrue(result.getStderr().startsWith("Missing required parameter: '"));
    }

    @Test
    @JFmtTest(args = { "list", "src/test/resources/correctly_formatted" })
    @DisplayName("runs list command with correctly formatted files should exit 0 and produce no output")
    void runs_list_command_with_path(JdtResult result) {
        assertEquals(0, result.exitCode());
        assertTrue(result.getStdout().isEmpty() || !result.getStdout().contains(".java"));
    }

    @Test
    @JFmtTest(args = { "list", "--config=equalsverifier", "src/test/resources/incorrectly_formatted/" })
    @DisplayName("runs list command with path should exit 1 and print 'not formatted correctly'")
    void runs_list_command_with_incorrectly_formatted_path(JdtResult result) {
        assertEquals(1, result.exitCode());
        assertTrue(result.getStdout().contains("src/test/resources/incorrectly_formatted"));
    }
}
