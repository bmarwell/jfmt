package io.github.bmarwell.jfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.jfmt.its.extension.JFmtTest;
import io.github.bmarwell.jfmt.its.extension.JdtResult;
import org.junit.jupiter.api.Test;

@JFmtTest
class CharsetSupportNativeIT extends AbstractCharsetIT {

    @Test
    @JFmtTest(args = { "print", "target/test-classes/charset/Cp1252Test.java" })
    void test_print_command_handles_cp1252_charset_on_native_binary(JdtResult result) {
        assertEquals(0, result.exitCode(), "Command should exit successfully");
        assertFalse(result.getStderr().contains("UnsupportedCharsetException"));

        String stdout = result.getStdout();
        assertFalse(stdout.isEmpty(), "Output should not be empty");
        assertTrue(stdout.contains("public class Cp1252Test"), "Should contain class declaration");
    }

    @Test
    @JFmtTest(args = { "diff", "target/test-classes/charset/Cp1252Test.java" })
    void test_diff_command_handles_cp1252_charset_on_native_binary(JdtResult result) {
        assertEquals(0, result.exitCode(), "Command should exit successfully");
        assertFalse(result.getStderr().contains("UnsupportedCharsetException"));
        assertFalse(result.getStderr().contains("Cp1252"));
    }
}
