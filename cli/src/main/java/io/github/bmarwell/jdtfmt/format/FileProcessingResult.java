package io.github.bmarwell.jdtfmt.format;

import java.nio.file.Path;

public record FileProcessingResult(Path javaFile, boolean hasDiff, boolean changesWritten) {}
