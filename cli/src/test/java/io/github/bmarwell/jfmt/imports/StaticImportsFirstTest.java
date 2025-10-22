package io.github.bmarwell.jfmt.imports;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test that verifies static imports are placed first when using the default import order.
 * Simplified test that relies on the existing DefaultImportOrderTest infrastructure.
 */
class StaticImportsFirstTest extends ImportOrderProcessorTestBase {

    @Test
    void static_imports_should_be_first() {
        String result = runAndGetImportBlock();

        // Verify static imports appear before non-static imports
        String[] lines = result.split("\n");
        int firstStaticLine = -1;
        int firstNonStaticLine = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("import static ") && firstStaticLine == -1) {
                firstStaticLine = i;
            } else if (line.startsWith("import ") && !line.startsWith("import static ") && firstNonStaticLine == -1) {
                firstNonStaticLine = i;
            }
        }

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
