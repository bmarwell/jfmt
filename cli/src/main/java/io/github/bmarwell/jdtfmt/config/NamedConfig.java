package io.github.bmarwell.jdtfmt.config;

public enum NamedConfig {
    builtin("default-config.xml"),
    equalsverifier("equalsverifier.xml");

    private final String resourcePath;

    NamedConfig(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
