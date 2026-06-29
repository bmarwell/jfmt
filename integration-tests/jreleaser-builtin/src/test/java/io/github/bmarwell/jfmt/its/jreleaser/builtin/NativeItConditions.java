package io.github.bmarwell.jfmt.its.jreleaser.builtin;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Locale;

final class NativeItConditions {

    private NativeItConditions() {
        // utility class
    }

    static void assumeNativeProfileOnGraalVm() {
        final String nativeProfileActive = System.getProperty("jfmt.native.profile.active", "false");
        assumeTrue(
            "true".equalsIgnoreCase(nativeProfileActive),
            "Native integration tests run only with -Pnative (jfmt.native.profile.active=true)."
        );

        final String javaVendor = System.getProperty("java.vendor", "");
        final String vmVendor = System.getProperty("java.vm.vendor", "");
        final String vendorText = (javaVendor + " " + vmVendor).toLowerCase(Locale.ROOT);
        assumeTrue(
            vendorText.contains("graal"),
            "Native integration tests run only on GraalVM runtimes."
        );
    }
}
