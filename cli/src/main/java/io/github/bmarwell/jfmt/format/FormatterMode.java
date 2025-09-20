package io.github.bmarwell.jfmt.format;

public enum FormatterMode {
    LIST_FIRST(true),
    LIST(true),
    PRINT(false),
    WRITE(true),
    DIFF(false);

    private final boolean verbose;

    private FormatterMode(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean verbose() {
        return verbose;
    }
}
