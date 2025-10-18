package io.github.bmarwell.jfmt.imports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EclipseImportOrderTest extends ImportOrderProcessorTestBase {

    @Test
    void eclipse_profile_orders_imports_correctly() {
        String expected = String.join(
            "\n",
            "import static com.example.Util.CONSTANT;",
            "import static java.util.Collections.emptyList;",
            "import static org.junit.jupiter.api.Assertions.assertEquals;",
            "",
            "import java.io.File;",
            "import java.lang.String;",
            "import java.util.List;",
            "import javax.annotation.Nullable;",
            "",
            "import org.assertj.core.api.Assertions;",
            "",
            "import com.example.App;",
            "",
            "import a.b.c.Alpha;",
            "import z.y.Xray;",
            ""
        );

        String actual = runAndGetImportBlock();
        assertEquals(expected, actual);
    }

    @Override
    protected String getProfileName() {
        return "eclipse";
    }
}
