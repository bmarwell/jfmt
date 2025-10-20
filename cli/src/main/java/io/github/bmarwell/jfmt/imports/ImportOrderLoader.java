package io.github.bmarwell.jfmt.imports;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Utility to load Eclipse JDT import order tokens from resources or files. */
public class ImportOrderLoader {

    public ImportOrderLoader() {}

    public ImportOrderConfiguration loadFromResource(String resourcePath) {
        try (InputStream in = ImportOrderLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null)
                return ImportOrderConfiguration.empty();
            return readTokens(in);
        } catch (IOException e) {
            return ImportOrderConfiguration.empty();
        }
    }

    public ImportOrderConfiguration loadFromFile(Path propsFile) {
        try (InputStream in = Files.newInputStream(propsFile)) {
            return readTokens(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ImportOrderConfiguration readTokens(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(in);

        List<Map.Entry<Integer, String>> entries = new ArrayList<>();

        for (Map.Entry<Object, Object> e : props.entrySet()) {
            int idx = Integer.parseInt(e.getKey().toString().trim());
            String val = e.getValue().toString();
            entries.add(Map.entry(idx, val));
        }

        // make sure the user did not mess up the order :)
        entries.sort(Map.Entry.comparingByKey());

        // collect found groups
        List<ImportOrderConfiguration.ImportOrderGroup> result = new ArrayList<>(entries.size());

        for (Map.Entry<Integer, String> e : entries) {
            java.util.List<String> prefixes = Arrays.asList(e.getValue().split("\\|"));

            result.add(new ImportOrderConfiguration.ImportOrderGroup(prefixes));
        }

        return new ImportOrderConfiguration(List.copyOf(result));
    }
}
