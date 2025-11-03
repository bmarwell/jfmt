package io.github.bmarwell.jfmt.writer;

import java.io.PrintWriter;
import java.util.List;
import picocli.CommandLine;

public class OutputWriter {
    private final CommandLine.Help.Ansi ansiMode;
    private final VerbosityLevel verbosityLevel;
    private final PrintWriter out;
    private final PrintWriter err;

    public enum VerbosityLevel {
        SILENT, // Errors only
        DEFAULT, // Warnings and info
        VERBOSE // Warnings, info, and debug
    }

    public OutputWriter(
        CommandLine.Help.Ansi ansiMode,
        VerbosityLevel verbosityLevel,
        PrintWriter out,
        PrintWriter err
    ) {
        this.ansiMode = ansiMode;
        this.verbosityLevel = verbosityLevel;
        this.out = out;
        this.err = err;
    }

    public void output(String line) {
        out.println(line);
    }

    public void output(List<String> lines) {
        lines.forEach(this::output);
    }

    public void info(String prefix, String message) {
        if (verbosityLevel == VerbosityLevel.SILENT) {
            return;
        }

        err.println(ansiMode.string("@|bold,green " + prefix + ":|@ " + message));
    }

    public void debug(String prefix, String message) {
        if (verbosityLevel != VerbosityLevel.VERBOSE) {
            return;
        }

        err.println(ansiMode.string("@|bold,cyan " + prefix + ":|@ " + message));
    }

    public void warn(String prefix, String message) {
        if (verbosityLevel == VerbosityLevel.SILENT) {
            return;
        }

        err.println(ansiMode.string("@|bold,yellow " + prefix + ":|@ " + message));
    }

    /**
     * Prints error to stderr unconditionally.
     * Used for critical errors that must always be reported.
     */
    public void error(String prefix, String message) {
        err.println(ansiMode.string("@|bold,red " + prefix + ":|@ " + message));
        err.flush();
    }
}
