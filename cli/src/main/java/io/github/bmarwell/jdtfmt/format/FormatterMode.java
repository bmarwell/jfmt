package io.github.bmarwell.jdtfmt.format;

public enum FormatterMode {
    LIST_FIRST(true),
    LIST_ALL(true),
    DIFF_FIRST_NORMAL(false),
    DIFF_ALL_NORMAL(false),
    DIFF_FIRST_UNIFIED(false),
    DIFF_ALL_UNIFIED(false),
    PRINT(false),
    WRITE(true);

    private final boolean verbose;

    private FormatterMode(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean verbose() {
        return verbose;
    }
}
