package io.github.bmarwell.jfmt.imports;

import java.util.List;

/**
 * Configuration for the import order.
 * 
 * @param importOrderGroups
 *     each group is separated by a blank line. Each group itself consists of a list of import prefixes.
 *     While they must end with a dot, the configuration file does not include them.
 */
public record ImportOrderConfiguration(List<ImportOrderGroup> importOrderGroups) {

    public static ImportOrderConfiguration empty() {
        return new ImportOrderConfiguration(List.of());
    }

    public record ImportOrderGroup(List<String> prefixes) {}
}
