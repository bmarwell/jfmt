package io.github.bmarwell.jfmt.concurrency;

import io.github.bmarwell.jfmt.format.FileProcessingResult;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.StructuredTaskScope;

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
    StructuredTaskScope.Joiner<FileProcessingResult, List<FileProcessingResult>> {

    private final Collection<FileProcessingResult> results = new ConcurrentLinkedDeque<>();

    private final Collection<Throwable> exceptions = new ConcurrentLinkedDeque<>();

    /**
     * Returns true to cancel remaining unstarted tasks when:
     * - A task fails with an exception, OR
     * - A task completes with shouldContinue=false (--no-all mode)
     */
    @Override
    public boolean onComplete(StructuredTaskScope.Subtask<? extends FileProcessingResult> subtask) {
        StructuredTaskScope.Joiner.super.onComplete(subtask);

        if (subtask.state() == StructuredTaskScope.Subtask.State.FAILED) {
            exceptions.add(subtask.exception());
            return true;
        }

        FileProcessingResult result = subtask.get();
        this.results.add(result);

        return !result.shouldContinue();
    }

    /**
     * Returns stream of all subtasks. Throws if any task failed with exception.
     *
     * <p>Note: Does not throw for shouldContinue=false, allowing output to be printed.</p>
     */
    @Override
    public List<FileProcessingResult> result() throws Throwable {
        if (!this.exceptions.isEmpty()) {
            final var illegalStateException = new IllegalStateException("One or more tasks failed");
            this.exceptions.forEach(illegalStateException::addSuppressed);

            throw illegalStateException;
        }

        return List.copyOf(this.results);
    }
}
