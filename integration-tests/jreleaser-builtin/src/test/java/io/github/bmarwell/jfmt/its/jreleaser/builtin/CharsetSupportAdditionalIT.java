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

@JFmtTest
class CharsetSupportAdditionalIT {

    private static final Path UTF8_SOURCE_FILE = resolvePath("src/test/resources/charset/Cp1252Test.java");
    private static final Path CP1252_TEST_FILE = resolvePath("target/test-classes/charset/Cp1252Test.java");

    @BeforeAll
    static void prepareCp1252TestFile() throws IOException {
        NativeItConditions.assumeNativeProfileOnGraalVm();
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
