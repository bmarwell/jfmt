package io.github.bmarwell.jfmt.imports;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.bmarwell.jfmt.commands.ImportOrderProcessor;
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
 * Test to verify that static imports are placed before other imports when using
 * the default import order configuration, using a realistic import structure similar
 * to AbstractCommand.java.
 */
class AbstractCommandLikeImportOrderTest {

    @Test
    void static_imports_should_be_before_other_imports() throws Exception {
        // Load test file with AbstractCommand-like import structure
        String source;
        try (InputStream in = AbstractCommandLikeImportOrderTest.class.getClassLoader()
            .getResourceAsStream("imports/AbstractCommandLike.java")) {
            if (in == null) {
                fail("Test resource imports/AbstractCommandLike.java must exist");
            }
            source = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        // Parse the source
        CompilationUnit cu = parseCompilationUnit(source);

        // Prepare the working document
        IDocument workingDoc = new Document(source);

        // Load the defaultorder configuration
        NamedImportOrder nio = NamedImportOrder.valueOf("defaultorder");
        ImportOrderConfiguration tokens = new ImportOrderLoader().loadFromResource(nio.getResourcePath());

        // Process imports
        new ImportOrderProcessor(tokens).rewriteImportsIfAny(cu, workingDoc);

        // Extract the result
        String result = workingDoc.get();

        // Find where imports start and end
        String[] lines = result.split("\n");
        int firstImportLine = -1;
        int lastImportLine = -1;
        int firstStaticImportLine = -1;
        int firstNonStaticImportLine = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("import static ")) {
                if (firstImportLine == -1)
                    firstImportLine = i;
                if (firstStaticImportLine == -1)
                    firstStaticImportLine = i;
                lastImportLine = i;
            } else if (line.startsWith("import ") && !line.startsWith("import static ")) {
                if (firstImportLine == -1)
                    firstImportLine = i;
                if (firstNonStaticImportLine == -1)
                    firstNonStaticImportLine = i;
                lastImportLine = i;
            }
        }

        // Debug output
        System.out.println("First import line: " + firstImportLine);
        System.out.println("Last import line: " + lastImportLine);
        System.out.println("First static import line: " + firstStaticImportLine);
        System.out.println("First non-static import line: " + firstNonStaticImportLine);

        // Print the import block for debugging
        System.out.println("\nImport block after processing:");
        for (int i = firstImportLine; i <= lastImportLine; i++) {
            System.out.println(lines[i]);
        }

        // Verify: static imports must come BEFORE non-static imports
        if (firstStaticImportLine != -1 && firstNonStaticImportLine != -1) {
            assertTrue(
                firstStaticImportLine < firstNonStaticImportLine,
                String.format(
                    "Static imports should come before non-static imports. " +
                        "Static import at line %d, non-static import at line %d",
                    firstStaticImportLine,
                    firstNonStaticImportLine
                )
            );
        } else {
            fail("Could not find both static and non-static imports in the result");
        }
    }

    private static CompilationUnit parseCompilationUnit(String sourceCode) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(sourceCode.toCharArray());
        parser.setUnitName("AbstractCommandLike.java");
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
}
