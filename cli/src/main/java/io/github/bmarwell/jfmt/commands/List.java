package io.github.bmarwell.jfmt.commands;

import com.github.difflib.patch.Patch;
import io.github.bmarwell.jfmt.format.FileProcessingResult;
import io.github.bmarwell.jfmt.format.FormatterMode;
import java.nio.file.Path;
import picocli.CommandLine;

@CommandLine.Command(
    name = "list",
    description = """
                  List files which are not formatted correctly without \
                  printing a diff or writing changes.
                  All files are processed by default.""",
    mixinStandardHelpOptions = true
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
            // Return shouldContinue based on reportAll flag for fail-fast behavior
            // AbstractCommand skips printing for List mode to avoid duplicates
            return new FileProcessingResult(javaFile, true, false, this.globalOptions.reportAll());
        }

        return new FileProcessingResult(javaFile, false, false, true);
    }
}
