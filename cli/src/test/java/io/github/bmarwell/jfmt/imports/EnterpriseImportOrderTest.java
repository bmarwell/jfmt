package io.github.bmarwell.jfmt.imports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EnterpriseImportOrderTest extends ImportOrderProcessorTestBase {

    @Test
    void default_profile_orders_imports_correctly() {
        String expected = String.join(
            "\n",
            "import static com.example.Util.CONSTANT;",
            "import static java.util.Collections.emptyList;",
            "import static org.junit.jupiter.api.Assertions.assertEquals;",
            "",
            "import a.b.c.Alpha;",
            "import com.example.App;",
            "import org.assertj.core.api.Assertions;",
            "import z.y.Xray;",
            "",
            "import java.io.File;",
            "import java.lang.String;",
            "import java.util.List;",
            "import javax.annotation.Nullable;",
            "import jakarta.inject.Inject;",
            ""
        );

        String actual = runAndGetImportBlock();
        assertEquals(expected, actual);
    }

    @Override
    protected String getProfileName() {
        return "enterprise";
    }
}
