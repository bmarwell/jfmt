package io.github.bmarwell.jfmt.concurrency;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;

/**
 * Creates virtual threads with bounded concurrency using a semaphore.
 *
 * <p>Limits concurrent execution to avoid starting too many tasks before fail-fast can cancel
 * unstarted ones. Uses virtual threads for lightweight concurrency without OS thread overhead.</p>
 *
 * <p>The semaphore approach is simpler than using a fixed thread pool since StructuredTaskScope
 * expects a ThreadFactory, not an Executor.</p>
 */
public class BoundedVirtualThreadExecutor {

    private BoundedVirtualThreadExecutor() {
        // Utility class
    }

    /**
     * Creates a thread factory with bounded virtual thread concurrency.
     *
     * <p>Default limit is CPU cores * 1.5, reasonable for I/O-bound operations.
     *
     * @return thread factory limited to CPU cores * 1.5 concurrent tasks
     */
    public static ThreadFactory create() {
        int defaultLimit = (int) Math.ceil(Runtime.getRuntime().availableProcessors() * 1.5);
        return create(defaultLimit);
    }

    /**
     * Creates a thread factory with specified concurrency limit.
     *
     * @param maxConcurrency
     *     maximum number of concurrent virtual threads
     * @return bounded thread factory
     */
    public static ThreadFactory create(int maxConcurrency) {
        Semaphore semaphore = new Semaphore(maxConcurrency);
        ThreadFactory virtualFactory = Thread.ofVirtual().factory();

        return task -> {
            Runnable wrapped = () -> {
                try {
                    semaphore.acquire();
                    try {
                        task.run();
                    } finally {
                        semaphore.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };
            return virtualFactory.newThread(wrapped);
        };
    }
}
