package io.github.bmarwell.jfmt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import picocli.CommandLine;

public class VersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() throws Exception {
        // 1) Try Implementation-Version from manifest (if present)
        Package pkg = VersionProvider.class.getPackage();
        if (pkg != null) {
            String implVersion = pkg.getImplementationVersion();
            if (implVersion != null && !implVersion.isBlank()) {
                return new String[] { "jfmt " + implVersion };
            }
        }

        // 2) Fallback: read version.properties resource
        try (InputStream is = VersionProvider.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                String v = p.getProperty("version");
                String artifact = p.getProperty("artifactId", "jfmt");
                if (v != null && !v.isBlank()) {
                    return new String[] { artifact + " " + v.trim() };
                }
            }
        } catch (IOException ignored) {
            // best-effort only
        }

        // 3) Last fallback
        return new String[] { "jfmt (version unknown)" };
    }
}
