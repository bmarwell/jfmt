package io.github.bmarwell.jdtfmt.commands;

import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import io.github.bmarwell.jdtfmt.format.FileProcessingResult;
import io.github.bmarwell.jdtfmt.format.FormatterMode;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import picocli.CommandLine;

@CommandLine.Command(
        name = "diff",
        description = """
                      Output in diff format. Normal diff is used unless -u is also given."""
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

        getWriter().output(javaFile.toString());

        // print normal diff
        for (AbstractDelta<String> delta : patch.getDeltas()) {
            if (Objects.requireNonNull(delta.getType()) == DeltaType.EQUAL) {
                continue;
            }

            for (String line : delta.getSource().getLines()) {
                getWriter().output("-" + line);
            }

            for (String line : delta.getTarget().getLines()) {
                getWriter().output("+" + line);
            }
        }

        return new FileProcessingResult(javaFile, true, false, this.globalOptions.reportAll);
    }

    private FileProcessingResult unifiedDiff(Path javaFile, List<String> originalSourceLines, Patch<String> patch) {
        if (patch.getDeltas().isEmpty()) {
            return new FileProcessingResult(javaFile, false, false, true);
        }

        final List<String> theDiff = UnifiedDiffUtils.generateUnifiedDiff(
            javaFile.toString(),
            javaFile + ".new",
            originalSourceLines,
            patch,
            this.context
        );

        getWriter().output(theDiff);

        return new FileProcessingResult(javaFile, true, false, this.globalOptions.reportAll);
    }
}
