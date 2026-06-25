package io.github.bmarwell.jfmt.imports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Regression test for <a href="https://github.com/bmarwell/jfmt/issues/164">#164</a>.
 *
 * <p>When the import block contains a comment (or extra blank lines), reordering shrinks the
 * replaced region. Previously the processor mutated the document and then rewrote the now-stale
 * AST against it, which made JDT's TokenScanner run past EOF for any node after the imports
 * ("Document does not match the AST" / "End Of File"). The source here therefore has a class body
 * after the imports, which the empty-bodied resources used by the other profile tests lack.
 */
class CommentBetweenImportsRegressionTest extends ImportOrderProcessorTestBase {

    @BeforeAll
    static void loadSource() throws IOException {
        try (InputStream in = ImportOrderProcessorTestBase.class.getClassLoader()
            .getResourceAsStream("imports/CommentBetweenImports.java")) {
            assertNotNull(in, "Test resource imports/CommentBetweenImports.java must exist");
            source = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void reordering_imports_with_a_comment_in_between_does_not_throw() {
        // runAndGetImportBlock() throws if the rewrite fails, so reaching the assertion proves #164 is fixed.
        String actual = runAndGetImportBlock();

        String expected = String.join(
            "\n",
            "import static java.lang.Math.PI;",
            "",
            "import com.example.App;",
            "import java.util.List;",
            ""
        );
        assertEquals(expected, actual);
    }

    @Override
    protected String getProfileName() {
        return "defaultorder";
    }
}
