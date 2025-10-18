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
    intellij
}
