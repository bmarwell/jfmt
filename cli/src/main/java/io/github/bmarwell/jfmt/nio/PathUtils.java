package io.github.bmarwell.jfmt.nio;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class PathUtils {

    public static Stream<Path> streamAll(List<Path> paths) {
        return paths.parallelStream()
            .flatMap(PathUtils::resolveAsStream);
    }

    public static Stream<Path> resolveAsStream(Path path) {
        if (Files.isSymbolicLink(path)) {
            // skip for now
            return Stream.empty();
        }

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Path does not exist: " + path);
        }

        if (Files.isDirectory(path)) {
            return resolveDirectory(path);
        }

        if (Files.isRegularFile(path) && path.toString().endsWith(".java")) {
            return Stream.of(path);
        }

        throw new IllegalArgumentException("Path is not a file or directory: " + path);
    }

    private static Stream<Path> resolveDirectory(Path path) {
        try (var fileStream = Files.walk(path)) {
            return fileStream.parallel()
                .filter(p -> p.toString().endsWith(".java"))
                .filter(Files::isRegularFile);
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        }

    }
}
