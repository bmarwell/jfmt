package io.github.bmarwell.jfmt.imports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Regression test for <a href="https://github.com/bmarwell/jfmt/issues/164">#164</a>, exercising the
 * various comment shapes that can appear inside an import block and asserting that reordering keeps
 * each comment attached to the import JDT associates it with.
 *
 * <p>When the import block contains a comment, reordering shrinks the replaced region. Previously the
 * processor mutated the document and then rewrote the now-stale AST against it, which made JDT's
 * TokenScanner run past EOF for any node after the imports ("Document does not match the AST" / "End
 * Of File"). Every case therefore has a class after the imports, the node that used to trigger the
 * crash.
 *
 * <p>The source has no blank line between the last import and the class: the processor renders the
 * (reordered) import block before the JDT formatter runs, so keeping the input tight makes the raw
 * output easy to assert and focuses each case on comment placement.
 */
class CommentHandlingTest extends ImportOrderProcessorTestBase {

    record Case(String name, String input, String expected) {
        @Override
        public String toString() {
            return name;
        }
    }

    static Stream<Case> cases() {
        return Stream.of(
            new Case(
                "block comment between imports",
                """
                package com.example;

                import com.example.App;
                /* block comment on the static import */
                import static java.lang.Math.PI;
                import java.util.List;
                public class T {}
                """,
                """
                package com.example;

                /* block comment on the static import */
                import static java.lang.Math.PI;

                import com.example.App;
                import java.util.List;

                public class T {}
                """
            ),
            new Case(
                "multiple consecutive comments",
                """
                package com.example;

                import com.example.App;
                // first comment
                // second comment
                import static java.lang.Math.PI;
                import java.util.List;
                public class T {}
                """,
                """
                package com.example;

                // first comment
                // second comment
                import static java.lang.Math.PI;

                import com.example.App;
                import java.util.List;

                public class T {}
                """
            ),
            new Case(
                "comment at the beginning of the import block",
                """
                package com.example;

                // leading comment before first import
                import com.example.App;
                import static java.lang.Math.PI;
                import java.util.List;
                public class T {}
                """,
                """
                package com.example;

                import static java.lang.Math.PI;

                // leading comment before first import
                import com.example.App;
                import java.util.List;

                public class T {}
                """
            ),
            new Case(
                // A comment on the line after the last import is a leading comment of the class, not
                // a trailing comment of the import, so JDT does not move it: it is preserved in place.
                "comment at the end of the import block",
                """
                package com.example;

                import com.example.App;
                import static java.lang.Math.PI;
                import java.util.List;
                // trailing comment after last import
                public class T {}
                """,
                """
                package com.example;

                import static java.lang.Math.PI;

                import com.example.App;
                import java.util.List;

                // trailing comment after last import
                public class T {}
                """
            ),
            new Case(
                "dangling javadoc-style comment",
                """
                package com.example;

                import com.example.App;
                /** dangling javadoc on the static import */
                import static java.lang.Math.PI;
                import java.util.List;
                public class T {}
                """,
                """
                package com.example;

                /** dangling javadoc on the static import */
                import static java.lang.Math.PI;

                import com.example.App;
                import java.util.List;

                public class T {}
                """
            ),
            new Case(
                "markdown comment",
                """
                package com.example;

                import com.example.App;
                /// markdown comment on the static import
                import static java.lang.Math.PI;
                import java.util.List;
                public class T {}
                """,
                """
                package com.example;

                /// markdown comment on the static import
                import static java.lang.Math.PI;

                import com.example.App;
                import java.util.List;

                public class T {}
                """
            )
        );
    }

    @ParameterizedTest
    @MethodSource("cases")
    void reordering_keeps_each_comment_attached_to_its_import(Case testCase) {
        String actual = runAndGetDocument(testCase.input());

        assertEquals(testCase.expected(), actual);
    }

    @Override
    protected String getProfileName() {
        return "defaultorder";
    }
}
