package io.github.bmarwell.jfmt.commands;

import picocli.CommandLine;

public class DiffOptions {

    @CommandLine.Option(
        names = { "-u", "--unified" },
        description = """
                      Output diff in unified format.
                      Deactivated by default.""",
        defaultValue = "false"
    )
    public boolean unified;

    @CommandLine.Option(
        names = { "--context" },
        description = """
                      Number of context lines when in unified diff mode (-u). Defaults to ${DEFAULT-VALUE}.""",
        defaultValue = "3"
    )
    public int context;
}
