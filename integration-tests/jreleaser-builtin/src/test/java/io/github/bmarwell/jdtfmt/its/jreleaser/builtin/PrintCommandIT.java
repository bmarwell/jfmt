package io.github.bmarwell.jdtfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.jdtfmt.its.extension.JdtFmtTest;
import io.github.bmarwell.jdtfmt.its.extension.JdtResult;
import org.junit.jupiter.api.Test;

@JdtFmtTest
class PrintCommandIT {

    @Test
    @JdtFmtTest(args = { "print", "src/test/resources/incorrectly_formatted/SomeRecord.java" })
    void test_print_command_prints_diff(JdtResult result) {
        // then
        assertTrue(result.getStderr().isEmpty());
        assertFalse(result.getStdout().isEmpty());

        assertTrue(result.getStdout().startsWith("/**"));
        assertTrue(result.getStdout().contains("public record SomeRecord("));
        assertTrue(result.getStdout().contains("public String getRecord() {"));
    }
}
