package io.github.bmarwell.jfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.jfmt.its.extension.JFmtTest;
import io.github.bmarwell.jfmt.its.extension.JdtResult;
import org.junit.jupiter.api.Test;

@JFmtTest
class PrintCommandIT {

    @Test
    @JFmtTest(args = { "print", "src/test/resources/incorrectly_formatted/SomeRecord.java" })
    void test_print_command_prints_diff(JdtResult result) {
        // then
        assertTrue(result.getStderr().isEmpty());
        assertFalse(result.getStdout().isEmpty());

        assertTrue(result.getStdout().contains("/**"));
        assertTrue(result.getStdout().contains("public record SomeRecord("));
        assertTrue(result.getStdout().contains("public String getRecord() {"));
    }
}
