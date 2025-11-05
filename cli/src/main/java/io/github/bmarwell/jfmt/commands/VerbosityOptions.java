package io.github.bmarwell.jfmt.commands;

import picocli.CommandLine;

/**
 * Mixin for controlling output verbosity levels.
 *
 * <p>Provides mutually exclusive --verbose and --silent flags.
 * Default (no flags) shows warnings and info messages.
 */
public class VerbosityOptions {

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "0..1")
    VerbosityLevel verbosityLevel = new VerbosityLevel();

    static class VerbosityLevel {
        @CommandLine.Option(
            names = { "-v", "--verbose" },
            description = "Enable verbose output (info and debug messages)"
        )
        boolean verbose;

        @CommandLine.Option(
            names = { "-q", "--quiet", "--silent" },
            description = "Suppress informational output (errors only)"
        )
        boolean silent;
    }

    public boolean isVerbose() {
        return verbosityLevel.verbose;
    }

    public boolean isSilent() {
        return verbosityLevel.silent;
    }

    public boolean isDefault() {
        return !verbosityLevel.verbose && !verbosityLevel.silent;
    }
}
