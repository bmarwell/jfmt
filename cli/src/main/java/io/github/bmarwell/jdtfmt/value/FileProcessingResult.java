package io.github.bmarwell.jdtfmt.value;

import java.nio.file.Path;

public record FileProcessingResult(Path javaFile, boolean hasDiff) {}
