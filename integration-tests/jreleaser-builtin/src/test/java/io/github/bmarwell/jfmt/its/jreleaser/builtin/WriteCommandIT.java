package io.github.bmarwell.jfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.jfmt.its.extension.JFmtTest;
import io.github.bmarwell.jfmt.its.extension.JdtResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

@JFmtTest
public class WriteCommandIT {

    @Test
    @JFmtTest(args = { "write", "target/test-classes/write/t001/SomeRecord.java" })
    void write_file(JdtResult result) throws IOException {
        // given
        Path original = Paths.get("src/test/resources/write/t001/SomeRecord.java");
        Path fixed = Paths.get("target/test-classes/write/t001/SomeRecord.java");

        // then
        assertEquals(0, result.exitCode());
        assertTrue(Files.size(original) != Files.size(fixed));

        // ... assert no stacktrace
        assertFalse(
            result.getStderr().contains("io.github.bmarwell.jfmt"),
            "stderr should not contain a stacktrace"
        );
        assertFalse(
            result.getStdout().contains("io.github.bmarwell.jfmt"),
            "stderr should not contain a stacktrace"
        );
    }

    @Test
    @JFmtTest(args = { "write", "target/test-classes/write/t002/AlreadyFormatted.java" })
    void write_non_existend_file(JdtResult result) {
        // expect

        // ... non-zero exit code
        assertEquals(1, result.exitCode());

        // ... assert no stacktrace
        assertFalse(
            result.getStderr().contains("io.github.bmarwell.jfmt"),
            "stderr should not contain a stacktrace, but was: " + result.getStderr()
        );
        assertFalse(
            result.getStdout().contains("io.github.bmarwell.jfmt"),
            "stdout should not contain a stacktrace, but was: " + result.getStdout()
        );
    }

}
