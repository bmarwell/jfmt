package io.github.bmarwell.jfmt.imports;

import static io.github.bmarwell.jfmt.imports.ImportOrderTestUtil.loadTestResource;
import static io.github.bmarwell.jfmt.imports.ImportOrderTestUtil.parseCompilationUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.bmarwell.jfmt.commands.ImportOrderProcessor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test to verify that the fallback behavior (when config is empty) matches
 * the default configuration behavior. Both should produce the same import order.
 */
class FallbackMatchesDefaultTest {

    private ImportOrderTestUtil.TestResource source;

    @BeforeEach
    void setUp() {
        source = loadTestResource("imports/MixedImports.java");
    }

    @Test
    void fallback_should_match_default_configuration() throws Exception {
        // given
        String resultWithDefault = processWithDefaultConfiguration();
        String resultWithFallback = processWithEmptyConfiguration();

        // when / then
        assertEquals(
            resultWithDefault,
            resultWithFallback,
            "Fallback behavior should match default configuration"
        );
    }

    private String processWithDefaultConfiguration() throws Exception {
        CompilationUnit cu = parseCompilationUnit(source.contents(), "MixedImports.java");
        IDocument workingDoc = new Document(source.contents());

        NamedImportOrder nio = NamedImportOrder.valueOf("defaultorder");
        ImportOrderConfiguration config = new ImportOrderLoader().loadFromResource(nio.getResourcePath());

        new ImportOrderProcessor(config).rewriteImportsIfAny(source.path(), cu, workingDoc);
        return workingDoc.get();
    }

    private String processWithEmptyConfiguration() throws Exception {
        CompilationUnit cu = parseCompilationUnit(source.contents(), "MixedImports.java");
        IDocument workingDoc = new Document(source.contents());

        ImportOrderConfiguration config = ImportOrderConfiguration.empty();

        new ImportOrderProcessor(config).rewriteImportsIfAny(source.path(), cu, workingDoc);
        return workingDoc.get();
    }
}
