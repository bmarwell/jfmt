package io.github.bmarwell.jfmt.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.eclipse.jdt.core.dom.ImportDeclaration;

/**
 * Represents a single import order group as configured by the Eclipse JDT
 * import order file. Holds a group name (token) and its collected imports.
 */
final class ImportOrderGroup {
    private final String name;
    private final List<ImportDeclaration> imports = new ArrayList<>();

    ImportOrderGroup(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void add(ImportDeclaration id) {
        this.imports.add(id);
    }

    public void addAll(List<ImportDeclaration> ids) {
        this.imports.addAll(ids);
    }

    public boolean isEmpty() {
        return this.imports.isEmpty();
    }

    public void sortByFqn() {
        this.imports.sort(Comparator.comparing(i -> i.getName().getFullyQualifiedName()));
    }

    public List<ImportDeclaration> elements() {
        return this.imports;
    }
}
