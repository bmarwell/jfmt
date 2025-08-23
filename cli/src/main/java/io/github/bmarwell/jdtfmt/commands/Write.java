package io.github.bmarwell.jdtfmt.commands;

import com.github.difflib.patch.Patch;
import io.github.bmarwell.jdtfmt.format.FileProcessingResult;
import io.github.bmarwell.jdtfmt.format.FormatterMode;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import picocli.CommandLine;

@CommandLine.Command(
        name = "write",
        aliases = { "fix" },
        description = """
                      Write the formatted source code back to the file(s).
                      If not given, the non-indented file(s) will be printed to stdout.
                      Only the first file is printed, unless -e is also given."""
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
}
