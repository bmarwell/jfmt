package io.github.bmarwell.jdtfmt.format;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import io.github.bmarwell.jdtfmt.writer.OutputWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

public class FormatProcessor {

    private final OutputWriter writer;
    private final FormatterMode fmtMode;
    private final DiffOptions diffOptions;

    public FormatProcessor(OutputWriter writer, FormatterMode fmtMode, DiffOptions diffOptions) {
        this.writer = writer;
        this.fmtMode = fmtMode;
        this.diffOptions = diffOptions;
    }

    public FileProcessingResult processRevisedSourceCode(Path javaFile, String sourceCode, String revisedSourceCode) {
        final List<String> originalSourceLines = List.of(sourceCode.split("\n"));
        final List<String> revisedSourceLines = List.of(revisedSourceCode.split("\n"));
        final Patch<String> patch = DiffUtils.diff(originalSourceLines, revisedSourceLines);

        return switch (this.fmtMode) {
            case PRINT -> printOutput(javaFile, revisedSourceLines, patch);
            case WRITE -> writeChange(javaFile, revisedSourceCode, patch);
            case LIST_FIRST, LIST_ALL -> listIfDiff(javaFile, patch);
            case DIFF_FIRST_NORMAL, DIFF_ALL_NORMAL -> diff(javaFile, patch);
            case DIFF_FIRST_UNIFIED, DIFF_ALL_UNIFIED -> diffUnified(javaFile, originalSourceLines, patch);
            default -> throw new UnsupportedOperationException("Unsupported formatter mode: " + this.fmtMode);
        };
    }

    private FileProcessingResult printOutput(Path javaFile, List<String> revisedSourceCodeLines, Patch<String> patch) {
        writer.output(revisedSourceCodeLines);

        if (patch.getDeltas().isEmpty()) {
            return new FileProcessingResult(javaFile, false, false);
        }

        return new FileProcessingResult(javaFile, true, false);
    }

    private FileProcessingResult diff(Path javaFile, Patch<String> patch) {
        if (patch.getDeltas().isEmpty()) {
            return new FileProcessingResult(javaFile, false, false);
        }

        // print normal diff
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            if (Objects.requireNonNull(delta.getType()) == DeltaType.EQUAL) {
                continue;
            }

            for (String line : delta.getSource().getLines()) {
                writer.output("<" + line);
            }

            for (String line : delta.getTarget().getLines()) {
                writer.output(">" + line);
            }
        }

        return new FileProcessingResult(javaFile, true, false);
    }

    private FileProcessingResult diffUnified(Path javaFile, List<String> originalSourceLines, Patch<String> patch) {
        if (patch.getDeltas().isEmpty()) {
            return new FileProcessingResult(javaFile, false, false);
        }

        final List<String> theDiff = UnifiedDiffUtils.generateUnifiedDiff(
            javaFile.toString(),
            javaFile + ".new",
            originalSourceLines,
            patch,
            this.diffOptions.unifiedDiffContextLine()
        );

        writer.output(theDiff);

        return new FileProcessingResult(javaFile, true, false);
    }

    private FileProcessingResult listIfDiff(Path javaFile, Patch<String> patch) {
        if (!patch.getDeltas().isEmpty()) {
            return new FileProcessingResult(javaFile, true, false);
        }

        return new FileProcessingResult(javaFile, false, false);
    }

    private static FileProcessingResult writeChange(Path javaFile, String revisedSourceCode, Patch<String> patch) {
        if (patch.getDeltas().isEmpty()) {
            return new FileProcessingResult(javaFile, false, false);
        }

        try (var os =
                Files.newOutputStream(javaFile, StandardOpenOption.TRUNCATE_EXISTING)) {
            os.write(revisedSourceCode.getBytes(StandardCharsets.UTF_8));

            return new FileProcessingResult(javaFile, true, true);
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        }
    }
}
