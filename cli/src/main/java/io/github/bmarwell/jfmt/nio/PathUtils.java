package io.github.bmarwell.jfmt.nio;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class PathUtils {

    private PathUtils() {}

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

        throw new IllegalArgumentException("Path is not a java file or directory: " + path);
    }

    /**
     * Caller must close the stream.
     * 
     * @param path
     *     the path to walk into recursively.
     * @return an open stream of .java files.
     */
    @SuppressWarnings("resource")
    private static Stream<Path> resolveDirectory(Path path) {
        try {
            return Files.walk(path)
                .parallel()
                .filter(p -> p.toString().endsWith(".java"))
                .filter(Files::isRegularFile);
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        }

    }
}
