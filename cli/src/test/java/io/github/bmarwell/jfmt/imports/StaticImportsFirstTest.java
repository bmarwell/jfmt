package io.github.bmarwell.jfmt.imports;

import static io.github.bmarwell.jfmt.imports.ImportOrderTestUtil.findFirstNonStaticImportLine;
import static io.github.bmarwell.jfmt.imports.ImportOrderTestUtil.findFirstStaticImportLine;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test that verifies static imports are placed first when using the default import order.
 * Simplified test that relies on the existing DefaultImportOrderTest infrastructure.
 */
class StaticImportsFirstTest extends ImportOrderProcessorTestBase {

    @Test
    void static_imports_should_be_first() {
        // given / when
        String result = runAndGetImportBlock();

        // then
        String[] lines = result.split("\n");
        int firstStaticLine = findFirstStaticImportLine(lines);
        int firstNonStaticLine = findFirstNonStaticImportLine(lines);

        assertTrue(
            firstStaticLine >= 0 && firstNonStaticLine >= 0 && firstStaticLine < firstNonStaticLine,
            "Static imports should appear before non-static imports"
        );
    }

    @Override
    protected String getProfileName() {
        return "defaultorder";
    }
}
