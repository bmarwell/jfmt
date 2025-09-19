package io.github.bmarwell.jfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.bmarwell.jfmt.its.extension.JFmtTest;
import io.github.bmarwell.jfmt.its.extension.JdtResult;
import org.junit.jupiter.api.Test;

@JFmtTest
public class HelpCommandIT {

    @Test
    @JFmtTest(args = { "--help" })
    void runHelp(JdtResult result) {
        assertEquals(0, result.exitCode());
    }
}
