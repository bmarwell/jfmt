package io.github.bmarwell.jfmt.imports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ImportOrderLoaderTest {

    @Test
    void canReadGroupsWithinGroups() throws IOException {
        var in = new ByteArrayInputStream("1=\\#\n2=\n3=java|javax|jakarta".getBytes(StandardCharsets.UTF_8));

        // when
        ImportOrderConfiguration configuration = new ImportOrderLoader().readTokens(in);

        // then
        assertEquals(3, configuration.importOrderGroups().size());
        ImportOrderConfiguration.ImportOrderGroup last = configuration.importOrderGroups().getLast();
        assertEquals(3, last.prefixes().size(), "should have 3 prefixes");
        assertEquals("java", last.prefixes().getFirst(), "should have java prefix");
        assertEquals("jakarta", last.prefixes().getLast(), "should have jakarta prefix last");
    }
}
