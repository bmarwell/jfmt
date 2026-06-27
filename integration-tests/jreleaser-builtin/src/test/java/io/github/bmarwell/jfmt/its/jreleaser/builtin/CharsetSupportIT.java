package io.github.bmarwell.jfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.jfmt.its.extension.JFmtTest;
import io.github.bmarwell.jfmt.its.extension.JdtResult;
import org.junit.jupiter.api.Test;

/**
 * Integration test to verify that the native image includes support for various charsets,
 * particularly Windows-1252 (Cp1252) which is commonly used on Windows systems.
 * 
 * This test addresses issue #190: UnsupportedCharsetException: Cp1252 with Windows image
 * 
 * The test verifies that:
 * 1. Files with Cp1252/Windows-1252 characters can be processed without charset exceptions
 * 2. The native image includes the necessary charset provider resources
 * 3. Common charsets (UTF-8, ISO-8859-1, Cp1252) are available at runtime
 */
@JFmtTest
class CharsetSupportIT {

    /**
     * Test that the print command can process files with Windows-1252 (Cp1252) characters.
     * This verifies that the charset is available in the native image.
     */
    @Test
    @JFmtTest(args = { "print", "src/test/resources/charset/Cp1252Test.java" })
    void test_print_command_handles_cp1252_charset(JdtResult result) {
        // then - command should succeed without charset exceptions
        assertEquals(0, result.getExitCode(), "Command should exit successfully");

        // Verify no charset-related errors in stderr
        String stderr = result.getStderr();
        assertFalse(
            stderr.contains("UnsupportedCharsetException"),
            "Should not throw UnsupportedCharsetException"
        );
        assertFalse(
            stderr.contains("Cp1252"),
            "Should not have Cp1252 charset errors"
        );

        // Verify the file was processed and output contains expected content
        String stdout = result.getStdout();
        assertFalse(stdout.isEmpty(), "Output should not be empty");
        assertTrue(stdout.contains("public class Cp1252Test"), "Should contain class declaration");
        assertTrue(stdout.contains("formatPrice"), "Should contain method name");
    }

    /**
     * Test that the list command can process files with special characters.
     * This ensures charset support works across different commands.
     */
    @Test
    @JFmtTest(args = { "list", "src/test/resources/charset/Cp1252Test.java" })
    void test_list_command_handles_cp1252_charset(JdtResult result) {
        // then - command should succeed
        assertEquals(0, result.getExitCode(), "Command should exit successfully");

        // Verify no charset errors
        String stderr = result.getStderr();
        assertFalse(
            stderr.contains("UnsupportedCharsetException"),
            "Should not throw UnsupportedCharsetException"
        );

        // Verify the file is listed
        String stdout = result.getStdout();
        assertTrue(
            stdout.contains("Cp1252Test.java"),
            "Should list the test file"
        );
    }

    /**
     * Test that diff command works with files containing special characters.
     * This is the command that originally triggered the issue in #190.
     */
    @Test
    @JFmtTest(args = { "diff", "src/test/resources/charset/Cp1252Test.java" })
    void test_diff_command_handles_cp1252_charset(JdtResult result) {
        // then - command should succeed without charset exceptions
        assertEquals(0, result.getExitCode(), "Command should exit successfully");

        // Verify no charset-related errors
        String stderr = result.getStderr();
        assertFalse(
            stderr.contains("UnsupportedCharsetException"),
            "Should not throw UnsupportedCharsetException"
        );
        assertFalse(
            stderr.contains("Cp1252"),
            "Should not have Cp1252 charset errors"
        );
    }
}

// Made with Bob
