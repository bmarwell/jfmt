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
 *
 * <p>It also verifies that a comment attached to an import is carried along with that import when
 * the imports are reordered, instead of being dropped.
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
    void reordering_imports_carries_the_comment_along_with_its_import() {
        String expected = """
                          package com.example;

                          // this comment is attached to the static import
                          import static java.lang.Math.PI;

                          import com.example.App;
                          import java.util.List;

                          public class CommentBetweenImports {
                              public double area(List<Integer> ignored) {
                                  App app = new App();
                                  return PI;
                              }
                          }
                          """;

        String actual = runAndGetDocument();

        assertEquals(expected, actual);
    }

    @Override
    protected String getProfileName() {
        return "defaultorder";
    }
}
