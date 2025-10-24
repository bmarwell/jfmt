package io.github.bmarwell.jfmt.format;

import java.nio.file.Path;
import java.util.List;

public record FileProcessingResult(
    Path javaFile,
    boolean hasDiff,
    boolean changesWritten,
    boolean shouldContinue,
    List<String> outputLines
) {
    public FileProcessingResult(Path javaFile, boolean hasDiff, boolean changesWritten, boolean shouldContinue) {
        this(javaFile, hasDiff, changesWritten, shouldContinue, List.of());
    }
}
