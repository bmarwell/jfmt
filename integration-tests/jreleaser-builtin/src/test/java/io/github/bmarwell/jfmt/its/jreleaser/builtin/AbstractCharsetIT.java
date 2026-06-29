package io.github.bmarwell.jfmt.its.jreleaser.builtin;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;

abstract class AbstractCharsetIT {

    protected static final Path UTF8_SOURCE_FILE = resolvePath("src/test/resources/charset/Cp1252Test.java");
    protected static final Path CP1252_TEST_FILE = resolvePath("target/test-classes/charset/Cp1252Test.java");

    @BeforeAll
    static void prepareCp1252TestFile() throws IOException {
        NativeItConditions.assumeNativeProfileOnGraalVm();
        Files.createDirectories(CP1252_TEST_FILE.getParent());

        final String source = Files.readString(UTF8_SOURCE_FILE, StandardCharsets.UTF_8);
        Files.writeString(CP1252_TEST_FILE, source, Charset.forName("windows-1252"));
    }

    protected static Path resolvePath(String moduleRelativePath) {
        final Path currentDirectory = Path.of("").toAbsolutePath();
        final Path directPath = currentDirectory.resolve(moduleRelativePath);

        if (Files.exists(directPath)) {
            return directPath;
        }

        return currentDirectory.resolve("integration-tests/jreleaser-builtin").resolve(moduleRelativePath);
    }
}
