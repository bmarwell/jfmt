package io.github.bmarwell.jfmt.concurrency;

import io.github.bmarwell.jfmt.format.FileProcessingResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Stream;

/**
 * Custom joiner for Structured Concurrency that cancels remaining tasks when shouldContinue=false.
 *
 * <p>Extends standard fail-fast behavior to also cancel on shouldContinue=false, not just exceptions.
 * This enables --no-all flag support: stop processing after first incorrectly formatted file.</p>
 *
 * <p>Unlike built-in joiners that throw exceptions, this returns all completed subtasks allowing
 * output from successful tasks to be printed before reporting failure.</p>
 */
public class FailFastFileProcessingResultJoiner implements
    StructuredTaskScope.Joiner<FileProcessingResult, Stream<StructuredTaskScope.Subtask<FileProcessingResult>>> {

    private final List<StructuredTaskScope.Subtask<FileProcessingResult>> subtasks = new ArrayList<>();

    private volatile Throwable firstException;

    @Override
    public boolean onFork(StructuredTaskScope.Subtask<? extends FileProcessingResult> subtask) {
        StructuredTaskScope.Joiner.super.onFork(subtask);

        @SuppressWarnings("unchecked")
        final var subtaskToAdd = (StructuredTaskScope.Subtask<FileProcessingResult>) subtask;
        this.subtasks.add(subtaskToAdd);

        return false;
    }

    /**
     * Returns true to cancel remaining unstarted tasks when:
     * - A task fails with an exception, OR
     * - A task completes with shouldContinue=false (--no-all mode)
     */
    @Override
    public boolean onComplete(StructuredTaskScope.Subtask<? extends FileProcessingResult> subtask) {
        StructuredTaskScope.Joiner.super.onComplete(subtask);

        return (subtask.state() == StructuredTaskScope.Subtask.State.FAILED
            && firstException == null
            && (firstException = subtask.exception()) != null)
            || !subtask.get().shouldContinue();
    }

    /**
     * Returns stream of all subtasks. Throws if any task failed with exception.
     *
     * <p>Note: Does not throw for shouldContinue=false, allowing output to be printed.</p>
     */
    @Override
    public Stream<StructuredTaskScope.Subtask<FileProcessingResult>> result() throws Throwable {
        Throwable ex = firstException;

        if (ex != null) {
            throw ex;
        }

        return subtasks.stream();
    }
}
