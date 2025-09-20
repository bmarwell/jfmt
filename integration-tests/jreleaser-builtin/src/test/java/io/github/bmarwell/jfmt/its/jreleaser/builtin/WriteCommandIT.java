package io.github.bmarwell.jfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    }
}
