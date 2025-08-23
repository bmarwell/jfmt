package io.github.bmarwell.jdtfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.bmarwell.jdtfmt.its.extension.JdtFmtTest;
import io.github.bmarwell.jdtfmt.its.extension.JdtResult;
import org.junit.jupiter.api.Test;

@JdtFmtTest
public class HelpCommandIT {

    @Test
    @JdtFmtTest(args = { "--help" })
    void runHelp(JdtResult result) {
        assertEquals(0, result.exitCode());
    }
}
