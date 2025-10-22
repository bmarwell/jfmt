package io.github.bmarwell.jfmt.concurrency;

import io.github.bmarwell.jfmt.format.FileProcessingResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Stream;

/**
 * Same as the default FailFastJoiner, but will also fail if no exception was thrown, but the subtask result is
 * {@code !shouldContinue()}.
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

        // always continue
        return false;
    }

    @Override
    public boolean onComplete(StructuredTaskScope.Subtask<? extends FileProcessingResult> subtask) {
        StructuredTaskScope.Joiner.super.onComplete(subtask);

        // cancel?
        // * on failed
        // * on !shouldContinue
        return (subtask.state() == StructuredTaskScope.Subtask.State.FAILED
            && firstException == null
            && (firstException = subtask.exception()) != null)
            || !subtask.get().shouldContinue();
    }

    @Override
    public Stream<StructuredTaskScope.Subtask<FileProcessingResult>> result() throws Throwable {
        Throwable ex = firstException;

        if (ex != null) {
            throw ex;
        }

        return subtasks.stream();
    }
}
