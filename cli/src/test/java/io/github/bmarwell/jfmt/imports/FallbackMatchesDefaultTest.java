package io.github.bmarwell.jfmt.imports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
 * Test to verify that the fallback behavior (when config is empty) matches
 * the default configuration behavior. Both should produce the same import order.
 */
class FallbackMatchesDefaultTest {

    @Test
    void fallback_should_match_default_configuration() throws Exception {
        // Load test file with mixed imports
        String source;
        try (InputStream in = FallbackMatchesDefaultTest.class.getClassLoader()
            .getResourceAsStream("imports/MixedImports.java")) {
            assertNotNull(in, "Test resource imports/MixedImports.java must exist");
            source = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        // Process with DEFAULT configuration
        String resultWithDefault = processWithConfiguration(source, false);

        // Process with EMPTY configuration (fallback)
        String resultWithFallback = processWithConfiguration(source, true);

        // Both should produce the same output
        assertEquals(
            resultWithDefault,
            resultWithFallback,
            "Fallback behavior should match default configuration"
        );
    }

    private String processWithConfiguration(String source, boolean useEmptyConfig) throws Exception {
        // Parse the source
        CompilationUnit cu = parseCompilationUnit(source);

        // Prepare the working document
        IDocument workingDoc = new Document(source);

        // Load configuration
        ImportOrderConfiguration config;
        if (useEmptyConfig) {
            // Use empty config to trigger fallback
            config = ImportOrderConfiguration.empty();
        } else {
            // Use default configuration
            NamedImportOrder nio = NamedImportOrder.valueOf("defaultorder");
            config = new ImportOrderLoader().loadFromResource(nio.getResourcePath());
        }

        // Process imports
        new ImportOrderProcessor(config).rewriteImportsIfAny(cu, workingDoc);

        return workingDoc.get();
    }

    private static CompilationUnit parseCompilationUnit(String sourceCode) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(sourceCode.toCharArray());
        parser.setUnitName("MixedImports.java");
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
