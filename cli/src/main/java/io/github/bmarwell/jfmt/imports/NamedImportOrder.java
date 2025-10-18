package io.github.bmarwell.jfmt.imports;

public enum NamedImportOrder {
    // default is a reserved keyword, so we need to use "defaultorder" instead. :(
    defaultorder("/io/github/bmarwell/jfmt/config/importorder.default.properties"),
    equalsverifier("/io/github/bmarwell/jfmt/config/importorder.equalsverifier.properties"),
    google("/io/github/bmarwell/jfmt/config/importorder.google.properties"),
    eclipse("/io/github/bmarwell/jfmt/config/importorder.eclipse.properties"),
    intellij("/io/github/bmarwell/jfmt/config/importorder.intellij.properties");

    private final String resourcePath;

    NamedImportOrder(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public static NamedImportOrder fromCli(CliNamedImportOrder cli) {
        return NamedImportOrder.valueOf(cli.name());
    }
}
