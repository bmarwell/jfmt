package io.github.bmarwell.jdtfmt.writer;

import java.util.List;
import picocli.CommandLine;

public class OutputWriter {
    private final CommandLine.Help.Ansi ansiMode;
    private final boolean verbose;

    public OutputWriter(CommandLine.Help.Ansi ansiMode, boolean verbose) {
        this.ansiMode = ansiMode;
        this.verbose = verbose;
    }

    public void output(String line) {
        System.out.println(line);
    }

    public void output(List<String> lines) {
        lines.forEach(this::output);
    }

    public void info(String prefix, String message) {
        if (verbose) {
            System.err.println(ansiMode.string("@|bold,green " + prefix + ":|@ " + message));
        }
    }

    public void warn(String prefix, String message) {
        if (verbose) {
            System.err.println(ansiMode.string("@|bold,red " + prefix + ":|@ " + message));
        }
    }
}
