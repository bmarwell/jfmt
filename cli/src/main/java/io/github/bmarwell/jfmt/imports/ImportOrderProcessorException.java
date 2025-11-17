package io.github.bmarwell.jfmt.imports;

import java.nio.file.Path;
import java.util.StringJoiner;

public class ImportOrderProcessorException extends RuntimeException {
    private final Path path;

    public ImportOrderProcessorException(Path path, String message, Throwable cause) {
        super(message, cause);
        this.path = path;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ImportOrderProcessorException.class.getSimpleName() + "[", "]")
            .add("super=" + super.toString())
            .add("path=" + path)
            .toString();
    }
}
