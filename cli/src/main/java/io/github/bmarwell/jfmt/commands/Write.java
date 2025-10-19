package io.github.bmarwell.jfmt.commands;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import io.github.bmarwell.jfmt.format.FileProcessingResult;
import io.github.bmarwell.jfmt.format.FormatterMode;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import picocli.CommandLine;

@CommandLine.Command(
    name = "write",
    aliases = { "fix" },
    description = """
                  Write the formatted source code back to the file(s).
                  If not given, the non-indented file(s) will be printed to stdout.
                  Only the first file is printed, unless -e is also given.""",
    mixinStandardHelpOptions = true
)
public class Write extends AbstractCommand {

    @Override
    FormatterMode getFormatterMode() {
        return FormatterMode.WRITE;
    }

    /**
     * Processes the revised source code by writing it back to the original file
     * if a patch indicates changes.
     * This method is called by {@code processSingleFile} and remains largely
     * as it was, handling the final write operation specific to the 'write' command.
     */
    @Override
    FileProcessingResult processRevisedSourceCode(
        Path javaFile,
        String sourceCode,
        String revisedSourceCode,
        List<String> originalSourceLines,
        List<String> revisedSourceLines,
        Patch<String> patch
    ) {
        if (patch.getDeltas().isEmpty()) {
            // No changes, so no write operation is needed.
            // Return a result indicating no change, but processed successfully.
            return new FileProcessingResult(javaFile, false, false, true);
        }

        try (var os =
            Files.newOutputStream(javaFile, StandardOpenOption.TRUNCATE_EXISTING)) {
            os.write(revisedSourceCode.getBytes(StandardCharsets.UTF_8));

            getWriter().info("Wrote formatted file", javaFile.toString());

            return new FileProcessingResult(javaFile, false, true, this.globalOptions.reportAll);
        } catch (IOException ioException) {
            // Wrap IOException in UncheckedIOException and re-throw,
            // which will be caught by the ExecutorService's ExecutionException handler.
            throw new UncheckedIOException(ioException);
        }
    }

    /**
     * Overrides the default call method to parallelize file processing using virtual threads.
     * Each file is processed by a separate virtual thread.
     * The results from each file are collected and aggregated.
     */
    @Override
    public Integer call() {
        // Results are collected in the main thread, so no need for a synchronized list.
        final List<FileProcessingResult> allResults = new ArrayList<>();

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            final List<Map.Entry<Path, Future<FileProcessingResult>>> futuresWithPaths = new ArrayList<>();

            // Submit all files for processing and collect their Futures.
            for (final Path inputFile : this.inputFiles) {
                final Future<FileProcessingResult> future = executor.submit(() -> this.processSingleFile(inputFile));
                futuresWithPaths.add(new AbstractMap.SimpleImmutableEntry<>(inputFile, future));
            }

            // Wait for all tasks to complete and collect results.
            for (final Map.Entry<Path, Future<FileProcessingResult>> entry : futuresWithPaths) {
                final Path inputFile = entry.getKey();
                final Future<FileProcessingResult> future = entry.getValue();
                try {
                    final FileProcessingResult result = future.get(); // Blocks until task completes or throws.
                    allResults.add(result);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore the interrupted status.
                    getWriter().error("Processing interrupted for file {}: {}", inputFile, e.getMessage());
                    allResults.add(new FileProcessingResult(inputFile, true, false, false, this.globalOptions.reportAll));
                } catch (ExecutionException e) {
                    // The actual exception thrown by processSingleFile is wrapped in ExecutionException.
                    final Throwable cause = e.getCause();
                    getWriter().error("Error processing file {}: {}", inputFile, cause.getMessage());
                    allResults.add(new FileProcessingResult(inputFile, true, false, false, this.globalOptions.reportAll));
                }
            }
        }

        // Aggregate all results to produce the final outcome and exit code.
        return super.aggregateResults(allResults);
    }

    /**
     * Encapsulates the logic for processing a single file: reading, formatting,
     * diffing, and delegating the final action to {@code processRevisedSourceCode}.
     *
     * @param inputFile The path to the Java file to process.
     * @return A {@link FileProcessingResult} indicating the outcome for this file.
     * @throws IOException if an I/O error occurs during reading or writing.
     */
    private FileProcessingResult processSingleFile(final Path inputFile) throws IOException {
        try {
            // Read the original source code.
            final String sourceCode = Files.readString(inputFile, StandardCharsets.UTF_8);
            final List<String> originalSourceLines = sourceCode.lines().toList();

            // Apply formatting. Assumes getFormatter() is inherited from AbstractCommand.
            final String revisedSourceCode = getFormatter().format(sourceCode);
            final List<String> revisedSourceLines = revisedSourceCode.lines().toList();

            // Compute the diff between original and revised lines.
            final Patch<String> patch = DiffUtils.diff(originalSourceLines, revisedSourceLines, null);

            // Delegate to the command's specific logic (Write.processRevisedSourceCode)
            // to handle the formatted code (i.e., write it back).
            return processRevisedSourceCode(
                inputFile,
                sourceCode,
                revisedSourceCode,
                originalSourceLines,
                revisedSourceLines,
                patch
            );
        } catch (UncheckedIOException e) {
            // Unwrap and re-throw the original IOException to be handled by the caller.
            throw e.getCause();
        }
    }
}