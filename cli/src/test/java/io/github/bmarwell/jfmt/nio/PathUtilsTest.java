package io.github.bmarwell.jfmt.nio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PathUtilsTest {

    @TempDir
    Path tempDir;

    Path textfile;

    Path linkToJavaFile;

    @BeforeEach
    void setUp() throws IOException {
        textfile = tempDir.resolve("textfile.java");
        linkToJavaFile = tempDir.resolve("linkToJavaFile.java");
        Files.writeString(textfile, "hello\n", StandardCharsets.UTF_8);
        Files.createSymbolicLink(linkToJavaFile, textfile);
    }

    @Test
    @DisplayName("symbolic links should resolve to an empty Stream")
    void symbolic_link_resolves_to_empty_stream() {
        // when
        Stream<Path> pathStream = PathUtils.resolveAsStream(linkToJavaFile);

        // then
        assertEquals(0L, pathStream.count());
    }

    @Test
    void throws_illegalArgumentException_when_path_is_not_a_file() {
        // expect
        assertThrows(IllegalArgumentException.class, () -> {
            PathUtils.resolveAsStream(tempDir.resolve("does_not_exist.java"));
        });
    }

    @Test
    @DisplayName("non-Java files should resolve to an empty Stream")
    void non_java_file_resolves_to_empty_stream() throws IOException {
        // given
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "hello\n", StandardCharsets.UTF_8);

        // when
        Stream<Path> pathStream = PathUtils.resolveAsStream(testFile);

        // then
        assertEquals(0L, pathStream.count());
    }
}
