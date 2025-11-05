package io.github.bmarwell.jfmt.commands;

import com.github.difflib.patch.Patch;
import io.github.bmarwell.jfmt.format.FileProcessingResult;
import io.github.bmarwell.jfmt.format.FormatterMode;
import java.nio.file.Path;
import java.util.List;
import picocli.CommandLine;

@CommandLine.Command(
    name = "print",
    description = """
                  Print the correctly formatted output for the given file(s).
                  All files are processed by default.
                  Use --no-all to stop after the first file.
                  When multiple files are processed, the file name is printed before each output.""",
    mixinStandardHelpOptions = true
)
public class Print extends AbstractCommand {

    @Override
    FormatterMode getFormatterMode() {
        return FormatterMode.PRINT;
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
            return new FileProcessingResult(javaFile, false, false, true, List.of(revisedSourceCode));
        }

        return new FileProcessingResult(
            javaFile,
            true,
            false,
            this.globalOptions.reportAll(),
            List.of(revisedSourceCode)
        );
    }
}
