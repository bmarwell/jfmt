package io.github.bmarwell.jdtfmt.commands;

import io.github.bmarwell.jdtfmt.config.CliNamedConfig;
import java.nio.file.Path;
import picocli.CommandLine;

public class GlobalOptions {

    @CommandLine.Parameters(
            description = """
                          Files or directory to scan and to format.""",
            arity = "1..*"
    )
    Path[] filesOrDirectories;

    @CommandLine.Option(
            names = { "-a", "--all" },
            description = """
                          Report all errors, not just the first one."""
    )
    boolean reportAll;

    @CommandLine.Option(
            names = { "--config" },
            description = """
                          Named config. Default: ${DEFAULT-VALUE}.
                          Available configs: ${COMPLETION-CANDIDATES}""",
            defaultValue = "builtin"
    )
    CliNamedConfig config = CliNamedConfig.builtin;

    @CommandLine.Option(
            names = { "--config-file" },
            description = """
                          Path to a config file. If unset (default), the named config (--config) will be used."""
    )
    Path configFile;

    @CommandLine.Option(
            names = { "--no-colour", "--no-color" },
            description = "Force no colored output, even if the terminal supports it."
    )
    public boolean noColor;

}
