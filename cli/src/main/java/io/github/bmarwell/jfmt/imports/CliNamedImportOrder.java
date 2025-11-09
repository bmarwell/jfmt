package io.github.bmarwell.jfmt.imports;

/**
 * CLI-facing enum for selectable import-order profiles.
 * Keeps names stable for PicoCLI completion-candidates, while the implementation
 * is provided by {@link NamedImportOrder}.
 */
public enum CliNamedImportOrder {
    defaultorder,
    equalsverifier,
    google,
    eclipse,
    intellij,
    enterprise,
    /**
     * Apache import order profile. First all javax non-static imports, then all java.* imports,
     * then all other non-static imports, then all static imports.
     */
    apache
}
