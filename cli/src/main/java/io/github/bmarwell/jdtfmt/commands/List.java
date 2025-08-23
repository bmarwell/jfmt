package io.github.bmarwell.jdtfmt.commands;

import com.github.difflib.patch.Patch;
import io.github.bmarwell.jdtfmt.format.FileProcessingResult;
import io.github.bmarwell.jdtfmt.format.FormatterMode;
import java.nio.file.Path;
import picocli.CommandLine;

@CommandLine.Command(
        name = "list",
        description = """
                      Just list files which are not formatted correctly without \
                      printing a diff or writing changes."""
)
public class List extends AbstractCommand {
    @Override
    FormatterMode getFormatterMode() {
        return FormatterMode.LIST;
    }

    @Override
    FileProcessingResult processRevisedSourceCode(
            Path javaFile,
            String sourceCode,
            String revisedSourceCode,
            java.util.List<String> originalSourceLines,
            java.util.List<String> revisedSourceLines,
            Patch<String> patch
    ) {
        if (!patch.getDeltas().isEmpty()) {
            getWriter().warn("Not formatted correctly", javaFile.toString());

            return new FileProcessingResult(javaFile, true, false, this.globalOptions.reportAll);
        }

        return new FileProcessingResult(javaFile, false, false, true);
    }
}
