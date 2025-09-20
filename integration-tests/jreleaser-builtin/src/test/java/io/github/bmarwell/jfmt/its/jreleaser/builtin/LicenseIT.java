package io.github.bmarwell.jfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class LicenseIT {

    @Test
    void licenses_exists_in_archive() {
        // given
        Path jdtDistributionPath = getJdtDistributionPath();

        Path asl2 = jdtDistributionPath.resolve("LICENSE.Apache-2.0");
        Path eupl = jdtDistributionPath.resolve("LICENSE.EUPL-1.2");
        Path thirdParty = jdtDistributionPath.resolve("etc").resolve("THIRD-PARTY.txt");

        // expect
        assertTrue(Files.exists(asl2), "LICENSE.Apache-2.0 must exist");
        assertTrue(Files.exists(eupl), "LICENSE.EUPL-1.2 must exist");
        assertTrue(Files.exists(thirdParty), "etc/THIRD-PARTY.txt must exist");
    }

    private static Path getJdtDistributionPath() {
        final String jdtFmtDirectory = System.getProperty("jfmt.directory");

        if (jdtFmtDirectory == null) {
            throw new IllegalStateException("jfmt.directory system property is not set.");
        }

        final Path jdtFmtPath = Paths.get(jdtFmtDirectory);

        if (!Files.exists(jdtFmtPath) || !Files.isDirectory(jdtFmtPath)) {
            throw new IllegalStateException("jfmt.directory system property is not set to a valid directory.");
        }

        return jdtFmtPath;
    }
}
