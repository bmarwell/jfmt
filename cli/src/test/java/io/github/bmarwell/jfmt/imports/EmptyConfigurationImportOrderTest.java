package io.github.bmarwell.jfmt.imports;

import static io.github.bmarwell.jfmt.imports.ImportOrderTestUtil.findFirstNonStaticImportLine;
import static io.github.bmarwell.jfmt.imports.ImportOrderTestUtil.findFirstStaticImportLine;
import static io.github.bmarwell.jfmt.imports.ImportOrderTestUtil.loadTestResource;
import static io.github.bmarwell.jfmt.imports.ImportOrderTestUtil.parseCompilationUnit;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bmarwell.jfmt.commands.ImportOrderProcessor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.api.Test;

/**
 * Test to verify behavior when import order configuration is empty,
 * which can happen if the properties file fails to load (e.g., in native images
 * if resources aren't properly configured).
 */
class EmptyConfigurationImportOrderTest {

    @Test
    void empty_configuration_should_place_static_imports_first() throws Exception {
        // given
        ImportOrderTestUtil.TestResource source = loadTestResource("imports/AbstractCommandLike.java");
        CompilationUnit cu = parseCompilationUnit(source.contents(), "AbstractCommandLike.java");
        IDocument workingDoc = new Document(source.contents());
        ImportOrderConfiguration emptyConfig = ImportOrderConfiguration.empty();

        // when
        new ImportOrderProcessor(emptyConfig).rewriteImportsIfAny(source.path(), cu, workingDoc);

        // then
        String[] lines = workingDoc.get().split("\n");
        int firstStaticLine = findFirstStaticImportLine(lines);
        int firstNonStaticLine = findFirstNonStaticImportLine(lines);

        assertTrue(firstStaticLine >= 0, "Should have static imports");
        assertTrue(firstNonStaticLine >= 0, "Should have non-static imports");
        assertTrue(
            firstStaticLine < firstNonStaticLine,
            String.format(
                "Static imports should come before non-static imports even with empty config. " +
                    "Static import at line %d, non-static import at line %d. " +
                    "This likely indicates the properties file failed to load (e.g., in native image).",
                firstStaticLine,
                firstNonStaticLine
            )
        );
    }
}
