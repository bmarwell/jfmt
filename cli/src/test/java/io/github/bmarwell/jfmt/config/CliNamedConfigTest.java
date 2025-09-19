package io.github.bmarwell.jfmt.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CliNamedConfigTest {

    @ParameterizedTest
    @EnumSource(CliNamedConfig.class)
    @DisplayName("one element must exist for each CLI argument")
    void one_element_must_exist_for_each_cli_argument(CliNamedConfig value) {
        final NamedConfig namedConfig = NamedConfig.valueOf(value.name());
        assertNotNull(namedConfig);
        assertNotNull(namedConfig.getResourcePath());
    }
}
