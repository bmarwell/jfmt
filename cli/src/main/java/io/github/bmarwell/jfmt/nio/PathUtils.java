package io.github.bmarwell.jfmt.nio;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PathUtils {

    public static Set<Path> resolveAll(List<Path> paths) {
        return paths.stream()
            .map(PathUtils::resolve)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    public static Set<Path> resolve(Path path) {
        if (Files.isSymbolicLink(path)) {
            // skip for now
            return Set.of();
        }

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Path does not exist: " + path);
        }

        if (Files.isDirectory(path)) {
            return resolveDirectory(path);
        }

        if (Files.isRegularFile(path) && path.toString().endsWith(".java")) {
            return Set.of(path);
        }

        throw new IllegalArgumentException("Path is not a file or directory: " + path);
    }

    private static Set<Path> resolveDirectory(Path path) {
        try (var fileStream = Files.walk(path)) {
            return fileStream
                .filter(p -> p.toString().endsWith(".java"))
                .filter(Files::isRegularFile)
                .collect(Collectors.toSet());
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        }

    }
}
