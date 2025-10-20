package io.github.bmarwell.jfmt.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.core.dom.ImportDeclaration;

/**
 * Represents a single import order group as configured by the Eclipse JDT
 * import order file. Holds a group name (token) and its collected imports.
 */
public final class ImportOrderGroup {
    private final String name;
    private final Map<String, List<ImportDeclaration>> importsByPackage = new ConcurrentHashMap<>();

    ImportOrderGroup(String name, List<String> packages) {
        this.name = name;
        packages.forEach(pkg -> importsByPackage.put(pkg, new ArrayList<>()));
    }

    ImportOrderGroup(List<String> packageNames) {
        this(String.join("|", packageNames), packageNames);
    }

    static ImportOrderGroup catchAll() {
        return new ImportOrderGroup("", List.of(""));
    }

    public String name() {
        return name;
    }

    public boolean acceptsImport(ImportDeclaration id) {
        return importsByPackage.keySet()
            .stream()
            .anyMatch(packageName -> id.getName().getFullyQualifiedName().startsWith(packageName));
    }

    public void add(ImportDeclaration id) {
        if (id.isStatic() && hasStaticPackage()) {
            addStatic(id);
            return;
        }

        if ((!acceptsImport(id)) && hasCatchAll()) {
            addCatchAll(id);
            return;
        }

        String packageGroupName = importsByPackage.keySet()
            .stream()
            .filter(packageName -> id.getName().getFullyQualifiedName().startsWith(packageName))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Import " + id.getName().getFullyQualifiedName() + " does not match any package group"
                )
            );

        this.importsByPackage.get(packageGroupName).add(id);
    }

    private void addCatchAll(ImportDeclaration id) {
        this.importsByPackage.get("").add(id);
    }

    private boolean hasCatchAll() {
        return this.importsByPackage.keySet()
            .stream()
            .anyMatch(String::isEmpty);
    }

    private void addStatic(ImportDeclaration id) {
        this.importsByPackage.get("#").add(id);
    }

    private boolean hasStaticPackage() {
        return this.importsByPackage.keySet()
            .stream()
            .anyMatch(packageName -> packageName.equals("#"));
    }

    public void addAll(List<ImportDeclaration> importDeclarations) {
        importDeclarations.forEach(this::add);
    }

    public boolean isEmpty() {
        return this.importsByPackage.isEmpty() || this.importsByPackage.values().stream().allMatch(List::isEmpty);
    }

    public void sortByFqn() {
        this.importsByPackage.values()
            .forEach(idl -> idl.sort(Comparator.comparing(i -> i.getName().getFullyQualifiedName())));
    }

    public List<ImportDeclaration> elements() {
        return this.importsByPackage.values()
            .stream()
            .flatMap(List::stream)
            .toList();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ImportOrderGroup.class.getSimpleName() + "[", "]")
            .add("name='" + name + "'")
            .add("importsByPackage=" + importsByPackage)
            .toString();
    }
}
