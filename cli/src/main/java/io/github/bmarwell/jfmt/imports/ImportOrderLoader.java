package io.github.bmarwell.jfmt.imports;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Utility to load Eclipse JDT import order tokens from resources or files. */
public final class ImportOrderLoader {
    private ImportOrderLoader() {}

    public static List<String> loadFromResource(String resourcePath) {
        try (InputStream in = ImportOrderLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null)
                return List.of();
            return readTokens(in);
        } catch (IOException e) {
            return List.of();
        }
    }

    public static List<String> loadFromFile(Path propsFile) {
        try (InputStream in = Files.newInputStream(propsFile)) {
            return readTokens(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<String> readTokens(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(in);
        List<Map.Entry<Integer, String>> entries = new ArrayList<>();
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            int idx = Integer.parseInt(e.getKey().toString().trim());
            String val = e.getValue().toString();
            entries.add(Map.entry(idx, val));
        }
        entries.sort(Map.Entry.comparingByKey());

        List<String> result = new ArrayList<>(entries.size());
        for (Map.Entry<Integer, String> e : entries) {
            result.add(e.getValue());
        }
        return List.copyOf(result);
    }
}
