package io.github.bmarwell.jfmt.commands;

import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import io.github.bmarwell.jfmt.format.FileProcessingResult;
import io.github.bmarwell.jfmt.format.FormatterMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import picocli.CommandLine;

@CommandLine.Command(
    name = "diff",
    description = """
                  Output in diff format. Normal diff is used unless -u is also given.
                  All files are processed by default.""",
    mixinStandardHelpOptions = true
)
public class Diff extends AbstractCommand {

    @CommandLine.Option(
        names = { "-u", "--unified" },
        description = """
                      Output diff in unified format.
                      Deactivated by default.""",
        defaultValue = "false"
    )
    private boolean unified;

    @CommandLine.Option(
        names = { "--context" },
        description = """
                      Number of context lines when in unified diff mode (-u). Defaults to ${DEFAULT-VALUE}.""",
        defaultValue = "3"
    )
    private int context;

    @Override
    FormatterMode getFormatterMode() {
        return FormatterMode.DIFF;
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
        if (unified) {
            return unifiedDiff(javaFile, originalSourceLines, patch);
        }

        return simpleDiff(javaFile, patch);
    }

    private FileProcessingResult simpleDiff(Path javaFile, Patch<String> patch) {
        if (patch.getDeltas().isEmpty()) {
            return new FileProcessingResult(javaFile, false, false, true);
        }

        final ArrayList<String> output = new java.util.ArrayList<>();
        output.add(javaFile.toString());

        // print normal diff
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            if (Objects.requireNonNull(delta.getType()) == DeltaType.EQUAL) {
                continue;
            }

            output.add(delta.getSource().getPosition() + "c" + delta.getTarget().getPosition());

            for (String line : delta.getSource().getLines()) {
                output.add("< " + line);
            }

            output.add("---");

            for (String line : delta.getTarget().getLines()) {
                output.add("> " + line);
            }
        }

        return new FileProcessingResult(javaFile, true, false, this.globalOptions.reportAll, output);
    }

    private FileProcessingResult unifiedDiff(Path javaFile, List<String> originalSourceLines, Patch<String> patch) {
        if (patch.getDeltas().isEmpty()) {
            return new FileProcessingResult(javaFile, false, false, true);
        }

        // UnifiedDiffUtils.generateUnifiedDiff returns List<String> where each element is one line
        final List<String> theDiff = UnifiedDiffUtils.generateUnifiedDiff(
            javaFile.toString(),
            javaFile + ".new",
            originalSourceLines,
            patch,
            this.context
        );

        return new FileProcessingResult(javaFile, true, false, this.globalOptions.reportAll, theDiff);
    }
}
