package io.github.bmarwell.jfmt.format;

import java.nio.file.Path;

public record FileProcessingResult(Path javaFile, boolean hasDiff, boolean changesWritten, boolean shouldContinue) {}
