package io.github.bmarwell.jfmt.format;

import java.nio.file.Path;
import java.util.List;

/**
 * Result of processing a single Java source file.
 *
 * @param javaFile
 *     the file that was processed
 * @param hasDiff
 *     true if the formatted output differs from the original
 * @param changesWritten
 *     true if changes were written back to the file
 * @param shouldContinue
 *     false triggers fail-fast cancellation (--no-all mode) in custom joiner;
 *     true continues processing all files (--all, default).
 *     Required because Structured Concurrency's built-in joiners can't handle conditional
 *     cancellation without losing already-completed task outputs.
 * @param outputLines
 *     formatted output or diff lines to display
 */
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
