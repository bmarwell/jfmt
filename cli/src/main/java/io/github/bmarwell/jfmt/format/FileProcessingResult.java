package io.github.bmarwell.jfmt.format;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

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
 * @param exception
 *     {@link Nullable} exception that occurred during processing (e.g., syntax errors)
 */
public record FileProcessingResult(
    Path javaFile,
    boolean hasDiff,
    boolean changesWritten,
    boolean shouldContinue,
    List<String> outputLines,
    Optional<Exception> exception
) {
    public FileProcessingResult(Path javaFile, boolean hasDiff, boolean changesWritten, boolean shouldContinue) {
        this(javaFile, hasDiff, changesWritten, shouldContinue, List.of(), Optional.empty());
    }

    public FileProcessingResult(
        Path javaFile,
        boolean hasDiff,
        boolean changesWritten,
        boolean shouldContinue,
        List<String> outputLines
    ) {
        this(javaFile, hasDiff, changesWritten, shouldContinue, outputLines, Optional.empty());
    }
}
