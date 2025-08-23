package io.github.bmarwell.jdtfmt.writer;

import java.io.PrintWriter;
import java.util.List;
import picocli.CommandLine;

public class OutputWriter {
    private final CommandLine.Help.Ansi ansiMode;
    private final boolean verbose;
    private final PrintWriter out;
    private final PrintWriter err;

    public OutputWriter(CommandLine.Help.Ansi ansiMode, boolean verbose, PrintWriter out, PrintWriter err) {
        this.ansiMode = ansiMode;
        this.verbose = verbose;
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
        if (verbose) {
            err.println(ansiMode.string("@|bold,green " + prefix + ":|@ " + message));
        }
    }

    public void warn(String prefix, String message) {
        if (verbose) {
            err.println(ansiMode.string("@|bold,red " + prefix + ":|@ " + message));
        }
    }
}
