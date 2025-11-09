package io.github.bmarwell.jfmt.imports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ApacheImportOrderTest extends ImportOrderProcessorTestBase {
    @Override
    protected String getProfileName() {
        return "apache";
    }

    @BeforeAll
    static void loadSource() throws IOException {
        try (InputStream in =
            ImportOrderProcessorTestBase.class.getClassLoader().getResourceAsStream("imports/MavenCore.java")) {
            assertNotNull(in, "Test resource imports/MavenCore.java must exist");
            source = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void does_not_change_as_expected() {
        // given
        String expected = """
                          import javax.inject.Inject;
                          import javax.inject.Named;
                          import javax.inject.Singleton;

                          import java.io.File;
                          import java.io.IOException;
                          import java.util.function.Function;
                          import java.util.stream.Stream;

                          import org.apache.maven.api.MonotonicClock;
                          import org.apache.maven.resolver.RepositorySystemSessionFactory;
                          import org.apache.maven.session.scope.internal.SessionScope;
                          import org.eclipse.aether.RepositorySystemSession;
                          import org.eclipse.aether.RepositorySystemSession.CloseableSession;
                          import org.eclipse.aether.repository.WorkspaceReader;
                          import org.eclipse.sisu.Nullable;
                          import org.slf4j.Logger;
                          import org.slf4j.LoggerFactory;
                          import org.slf4j.helpers.MessageFormatter;

                          import static java.util.stream.Collectors.toSet;
                          """;

        // when
        String actual = runAndGetImportBlock();

        // then
        assertEquals(expected, actual);
    }
}
