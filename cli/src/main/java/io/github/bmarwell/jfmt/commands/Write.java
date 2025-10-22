package io.github.bmarwell.jfmt.commands;

import com.github.difflib.patch.Patch;
import io.github.bmarwell.jfmt.format.FileProcessingResult;
import io.github.bmarwell.jfmt.format.FormatterMode;
import io.github.bmarwell.jfmt.nio.PathUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Stream;
import org.eclipse.jdt.core.formatter.CodeFormatter;
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
            return new FileProcessingResult(javaFile, false, false, true);
        }

        try (var os =
            Files.newOutputStream(javaFile, StandardOpenOption.TRUNCATE_EXISTING)) {
            os.write(revisedSourceCode.getBytes(StandardCharsets.UTF_8));

            getWriter().info("Wrote formatted file", javaFile.toString());

            return new FileProcessingResult(javaFile, false, true, this.globalOptions.reportAll);
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        }
    }

    @Override
    public Integer call() throws Exception {
        final Stream<Path> allFilesAndDirs = PathUtils.streamAll(List.of(this.globalOptions.filesOrDirectories));
        final CodeFormatter formatter = createCodeFormatter();
        final ArrayList<FileProcessingResult> results = new ArrayList<>();

        try (var scope =
            StructuredTaskScope.open(StructuredTaskScope.Joiner.<FileProcessingResult>allSuccessfulOrThrow())) {
            allFilesAndDirs
                .map(javaFile -> (Callable<FileProcessingResult>) () -> processFile(formatter, javaFile))
                .forEach(scope::fork);

            final List<StructuredTaskScope.Subtask<FileProcessingResult>> joins = scope.join().toList();

            for (StructuredTaskScope.Subtask<FileProcessingResult> join : joins) {
                if (join.exception() != null) {
                    throw (Exception) join.exception();
                }

                results.add(join.get());
            }
        }

        return results.stream().anyMatch(FileProcessingResult::hasDiff) ? 1 : 0;
    }
}
