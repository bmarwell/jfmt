package io.github.bmarwell.jdtfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.jdtfmt.its.extension.JdtFmtTest;
import io.github.bmarwell.jdtfmt.its.extension.JdtResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

@JdtFmtTest
public class WriteCommandIT {

    @Test
    @JdtFmtTest(args = { "write", "target/test-classes/write/t001/SomeRecord.java" })
    void write_file(JdtResult result) throws IOException {
        // given
        Path original = Paths.get("src/test/resources/write/t001/SomeRecord.java");
        Path fixed = Paths.get("target/test-classes/write/t001/SomeRecord.java");

        // then
        assertEquals(0, result.exitCode());
        assertTrue(Files.size(original) != Files.size(fixed));
    }
}
