package io.github.bmarwell.jfmt.commands;

import io.github.bmarwell.jfmt.config.CliNamedConfig;
import io.github.bmarwell.jfmt.imports.CliNamedImportOrder;
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
        names = { "--no-all" },
        description = """
                      Stop after the first file with differences.
                      By default, all files are processed."""
    )
    boolean noAll = false;

    // reportAll is true (process all files) unless --no-all is specified
    public boolean reportAll = true;

    // Update reportAll after parsing based on noAll flag
    void updateReportAll() {
        this.reportAll = !this.noAll;
    }

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

    @CommandLine.Option(
        names = { "--import-order" },
        description = "Named import order. Default: ${DEFAULT-VALUE}. Available: ${COMPLETION-CANDIDATES}",
        defaultValue = "defaultorder"
    )
    public CliNamedImportOrder importOrder = CliNamedImportOrder.defaultorder;

    @CommandLine.Option(
        names = { "--import-order-file" },
        description = "Path to an import-order properties file. If set, overrides --import-order."
    )
    public Path importOrderFile;

    @CommandLine.Mixin
    public VerbosityOptions verbosityOptions = new VerbosityOptions();
}
