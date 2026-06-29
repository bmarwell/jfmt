package io.github.bmarwell.jfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.jfmt.its.extension.JFmtTest;
import io.github.bmarwell.jfmt.its.extension.JdtResult;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
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

    private static final Path UTF8_SOURCE_FILE = resolvePath("src/test/resources/charset/Cp1252Test.java");
    private static final Path CP1252_TEST_FILE = resolvePath("target/test-classes/charset/Cp1252Test.java");

    @BeforeAll
    static void prepareCp1252TestFile() throws IOException {
        Files.createDirectories(CP1252_TEST_FILE.getParent());

        final String source = Files.readString(UTF8_SOURCE_FILE, StandardCharsets.UTF_8);
        Files.writeString(CP1252_TEST_FILE, source, Charset.forName("windows-1252"));
    }

    private static Path resolvePath(String moduleRelativePath) {
        final Path currentDirectory = Path.of("").toAbsolutePath();
        final Path directPath = currentDirectory.resolve(moduleRelativePath);

        if (Files.exists(directPath)) {
            return directPath;
        }

        return currentDirectory.resolve("integration-tests/jreleaser-builtin").resolve(moduleRelativePath);
    }

    /**
     * Test that the print command can process files with Windows-1252 (Cp1252) characters.
     * This verifies that the charset is available in the native image.
     */
    @Test
    @JFmtTest(args = { "print", "target/test-classes/charset/Cp1252Test.java" })
    void test_print_command_handles_cp1252_charset(JdtResult result) {
        // then - command should succeed without charset exceptions
        assertEquals(0, result.exitCode(), "Command should exit successfully");

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
    @JFmtTest(args = { "list", "target/test-classes/charset/Cp1252Test.java" })
    void test_list_command_handles_cp1252_charset(JdtResult result) {
        // then - command should succeed
        assertEquals(0, result.exitCode(), "Command should exit successfully");

        // Verify no charset errors
        String stderr = result.getStderr();
        assertFalse(
            stderr.contains("UnsupportedCharsetException"),
            "Should not throw UnsupportedCharsetException"
        );

        // Verify command produced output without crashing on charset handling
        assertFalse(result.getStdout().contains("UnsupportedCharsetException"));
    }

    /**
     * Test that diff command works with files containing special characters.
     * This is the command that originally triggered the issue in #190.
     */
    @Test
    @JFmtTest(args = { "diff", "target/test-classes/charset/Cp1252Test.java" })
    void test_diff_command_handles_cp1252_charset(JdtResult result) {
        // then - command should succeed without charset exceptions
        assertEquals(0, result.exitCode(), "Command should exit successfully");

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
