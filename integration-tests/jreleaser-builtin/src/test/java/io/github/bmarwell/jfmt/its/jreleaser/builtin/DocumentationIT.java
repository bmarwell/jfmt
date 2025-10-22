package io.github.bmarwell.jfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DocumentationIT {

    @Test
    @Disabled
    void generated_html_documentation_contains_syntax_highlighting() throws IOException {
        // given
        Path jdtDistributionPath = getJdtDistributionPath();
        Path htmlDoc = jdtDistributionPath.resolve("usr/share/doc/jfmt.html");

        // expect
        assertTrue(Files.exists(htmlDoc), "HTML documentation must exist at usr/share/doc/jfmt.html");

        // read the content and check for syntax highlighting
        String content = Files.readString(htmlDoc);

        // Check for syntax highlighting elements
        // Coderay adds classes like 'CodeRay' or elements with syntax highlighting
        assertTrue(
            content.contains("class=\"CodeRay\"")
                || content.contains("class=\"highlight\"")
                || content.contains("class=\"listingblock\""),
            "HTML documentation should contain syntax highlighting elements"
        );
    }

    private static Path getJdtDistributionPath() {
        final String jdtFmtDirectory = System.getProperty("jfmt.directory");

        if (jdtFmtDirectory == null) {
            throw new IllegalStateException("jfmt.directory system property is not set.");
        }

        final Path jdtFmtPath = Paths.get(jdtFmtDirectory);

        if (!Files.exists(jdtFmtPath) || !Files.isDirectory(jdtFmtPath)) {
            throw new IllegalStateException("jfmt.directory system property is not set to a valid directory.");
        }

        return jdtFmtPath;
    }
}
