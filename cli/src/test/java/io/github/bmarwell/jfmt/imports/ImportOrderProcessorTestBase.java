package io.github.bmarwell.jfmt.imports;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.bmarwell.jfmt.commands.ImportOrderProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.jupiter.api.BeforeAll;

abstract class ImportOrderProcessorTestBase {

    public static final String MIXED_IMPORTS_JAVA = "MixedImports.java";
    static String source;

    @BeforeAll
    static void loadSource() throws IOException {
        try (InputStream in =
            ImportOrderProcessorTestBase.class.getClassLoader().getResourceAsStream("imports/MixedImports.java")) {
            assertNotNull(in, "Test resource imports/MixedImports.java must exist");
            source = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected String runAndGetImportBlock() {
        try {
            // Parse the source into a CompilationUnit
            CompilationUnit cu = parseCompilationUnit(source);

            // Prepare the working document
            IDocument workingDoc = new Document(source);

            // Load tokens for the given profile
            NamedImportOrder nio = NamedImportOrder.valueOf(getProfileName());
            ImportOrderConfiguration tokens = new ImportOrderLoader().loadFromResource(nio.getResourcePath());

            // Rewrite imports according to tokens
            new ImportOrderProcessor(tokens).rewriteImportsIfAny(cu, workingDoc);

            // Extract and return the import block
            return extractImportBlock(workingDoc);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected abstract String getProfileName();

    private static CompilationUnit parseCompilationUnit(String sourceCode) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(sourceCode.toCharArray());
        parser.setUnitName(MIXED_IMPORTS_JAVA);
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

    private static String extractImportBlock(IDocument doc)
        throws MalformedTreeException {
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
