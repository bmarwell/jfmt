package io.github.bmarwell.jfmt;

import picocli.CommandLine;

public class JfmtOperationModeArgs {
    @CommandLine.Option(
        names = { "-l", "--list" },
        description = """
                      Do not print reformatted files to stdout.
                      Just report the name of the files which are not formatted correctly."""
    )
    public boolean listOnly;

    @CommandLine.Option(
        names = { "-s", "--stdout" },
        description = """
                      Print reformatted files to stdout instead of overwriting them.
                      Only the first file is printed, unless -a (all) is also given."""
    )
    public boolean stdout;

    @CommandLine.Option(
        names = { "-d", "--diff" },
        description = """
                      Print the diff to stdout.
                      Only the first file is printed, unless -a (all) is also given.
                      Use -u to switch to the unified diff format."""
    )
    public boolean diff;
}
