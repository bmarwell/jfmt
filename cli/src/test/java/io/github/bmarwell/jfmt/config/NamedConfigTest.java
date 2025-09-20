package io.github.bmarwell.jfmt.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class NamedConfigTest {

    @ParameterizedTest
    @EnumSource(NamedConfig.class)
    void each_resource_path_exists(NamedConfig value) {
        final InputStream valueResource = NamedConfig.class.getResourceAsStream(value.getResourcePath());

        assertNotNull(valueResource);
    }
}
