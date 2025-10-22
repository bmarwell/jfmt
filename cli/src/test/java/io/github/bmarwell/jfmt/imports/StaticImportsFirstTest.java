package io.github.bmarwell.jfmt.imports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.api.Test;

/**
 * Test that verifies static imports are moved to the beginning when using the default import order.
 * This test specifically checks the case where static imports are initially at the end.
 */
class StaticImportsFirstTest {

    @Test
    void static_imports_should_be_moved_to_beginning() throws Exception {
        // Load test file where static imports are at the END
        String source;
        try (InputStream in = StaticImportsFirstTest.class.getClassLoader()
            .getResourceAsStream("imports/StaticImportsAtEnd.java")) {
            assertNotNull(in, "Test resource imports/StaticImportsAtEnd.java must exist");
            source = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        // Parse the source into a CompilationUnit
        CompilationUnit cu = parseCompilationUnit(source);

        // Prepare the working document
        IDocument workingDoc = new Document(source);

        // Load tokens for the default profile
        NamedImportOrder nio = NamedImportOrder.valueOf("defaultorder");
        ImportOrderConfiguration tokens = new ImportOrderLoader().loadFromResource(nio.getResourcePath());

        // Rewrite imports according to default order
        new io.github.bmarwell.jfmt.commands.ImportOrderProcessor(tokens).rewriteImportsIfAny(cu, workingDoc);

        // Extract the import block
        String actual = extractImportBlock(workingDoc);

        // Static imports should be FIRST, then all others
        String expected = String.join(
            "\n",
            "import static com.example.Util.CONSTANT;",
            "import static java.util.Collections.emptyList;",
            "import static org.junit.jupiter.api.Assertions.assertEquals;",
            "",
            "import a.b.c.Alpha;",
            "import com.example.App;",
            "import jakarta.inject.Inject;",
            "import java.io.File;",
            "import java.lang.String;",
            "import java.util.List;",
            "import javax.annotation.Nullable;",
            "import org.assertj.core.api.Assertions;",
            "import z.y.Xray;",
            ""
        );

        assertEquals(expected, actual, "Static imports should be moved to the beginning");
    }

    private static CompilationUnit parseCompilationUnit(String sourceCode) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(sourceCode.toCharArray());
        parser.setUnitName("StaticImportsAtEnd.java");
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_SOURCE, String.valueOf(AST.getJLSLatest()));
        options.put(JavaCore.COMPILER_COMPLIANCE, String.valueOf(AST.getJLSLatest()));
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, String.valueOf(AST.getJLSLatest()));
        parser.setCompilerOptions(options);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        cu.recordModifications();

        return cu;
    }

    private static String extractImportBlock(IDocument doc) {
        String text = doc.get();
        String[] lines = text.split("\n", -1); // keep trailing empties

        int startIdx = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("import ")) {
                startIdx = i;
                break;
            }
        }

        if (startIdx < 0) {
            return "";
        }

        StringBuilder block = new StringBuilder();
        for (int idx = startIdx; idx < lines.length; idx++) {
            String line = lines[idx];
            if (line.startsWith("import ")) {
                block.append(line).append('\n');
                continue;
            }

            if (line.isBlank()) {
                // include blank lines only if followed by another import (group separator)
                if ((idx + 1) < lines.length && lines[idx + 1].startsWith("import ")) {
                    block.append('\n');
                    continue;
                }

                // trailing blank(s) before class: stop
                break;
            }

            break;
        }

        return block.toString();
    }
}
