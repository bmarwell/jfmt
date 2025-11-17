package io.github.bmarwell.jfmt.commands;

import io.github.bmarwell.jfmt.imports.ImportOrderConfiguration;
import io.github.bmarwell.jfmt.imports.ImportOrderProcessorException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/**
 * Encapsulates all logic for reading import-order configuration and reordering
 * imports of a parsed CompilationUnit accordingly.
 */
public class ImportOrderProcessor {

    private final ImportOrderConfiguration importOrderTokens;

    public ImportOrderProcessor(ImportOrderConfiguration importOrderTokens) {
        this.importOrderTokens = importOrderTokens;
    }

    /**
     * Rewrites the imports in the provided document according to the configured order.
     * If there are no imports, this method is a no-op.
     */
    public void rewriteImportsIfAny(Path javaFile, CompilationUnit compilationUnit, IDocument workingDoc)
        throws BadLocationException {
        @SuppressWarnings("unchecked")
        List<ImportDeclaration> imports = new ArrayList<>((List<ImportDeclaration>) compilationUnit.imports());
        if (imports.isEmpty()) {
            return;
        }

        // Partition into static and non-static once.
        Partition p = partitionImports(imports);

        // Decide configured vs fallback.
        List<ImportOrderGroup> groups = buildGroupsFromConfig(p);

        String rendered = renderGroups(groups);
        replaceImportsInDocument(javaFile, compilationUnit, workingDoc, rendered);
    }

    private Partition partitionImports(List<ImportDeclaration> imports) {
        List<ImportDeclaration> staticImports = new ArrayList<>();
        List<ImportDeclaration> nonStaticImports = new ArrayList<>();
        for (ImportDeclaration id : imports) {
            if (id.isStatic())
                staticImports.add(id);
            else
                nonStaticImports.add(id);
        }
        return new Partition(staticImports, nonStaticImports);
    }

    protected List<ImportOrderGroup> buildGroupsFromConfig(Partition p) {
        List<ImportDeclaration> remainingNonStatic = new ArrayList<>(p.nonStatic);
        // a group is defined by surrounding blank lines.
        // within each group, multiple import prefixes (domains) can exist.
        List<ImportOrderGroup> groups = new ArrayList<>();
        // empty token collects leftovers AFTER other tokens
        ImportOrderGroup othersGroup = null;
        boolean staticConfigured = false;

        for (ImportOrderConfiguration.ImportOrderGroup importOrderGroup : this.importOrderTokens.importOrderGroups()) {
            // unescape '#' if necessary
            String t =
                "\\#".equals(importOrderGroup.prefixes().getFirst()) ? "#" : importOrderGroup.prefixes().getFirst();

            if ("#".equals(t)) {
                // todo: what if others package groups exist in the group starting with '#'?
                ImportOrderGroup g = new ImportOrderGroup("#", List.of("#"));
                g.addAll(p.staticImports);
                groups.add(g);
                staticConfigured = true;
                continue;
            }

            if (t.isEmpty()) {
                // todo: what if others package groups exist in the group starting with catch-all?
                othersGroup = ImportOrderGroup.catchAll();
                groups.add(othersGroup);
                continue;
            }

            ImportOrderGroup g = new ImportOrderGroup(importOrderGroup.prefixes());
            for (Iterator<ImportDeclaration> it = remainingNonStatic.iterator(); it.hasNext();) {
                ImportDeclaration id = it.next();
                if (g.acceptsImport(id)) {
                    g.add(id);
                    it.remove();
                }
            }
            groups.add(g);
        }

        // If no static token configured, but we have static imports, prepend them as first group
        if (!staticConfigured && !p.staticImports.isEmpty()) {
            ImportOrderGroup g = new ImportOrderGroup("#", List.of("#"));
            g.addAll(p.staticImports);
            groups.addFirst(g);
        }

        // Fill others group with remaining non-static imports (if configured), otherwise create a trailing one
        if (othersGroup != null) {
            othersGroup.addAll(remainingNonStatic);
        } else if (!remainingNonStatic.isEmpty()) {
            // Fallback: match default config (0=#, 1=) - all non-static imports in one group
            ImportOrderGroup trailingOthers = ImportOrderGroup.catchAll();
            trailingOthers.addAll(remainingNonStatic);
            groups.add(trailingOthers);
        }

        return groups;
    }

    private String renderGroups(List<ImportOrderGroup> groups) {
        StringBuilder sb = new StringBuilder();
        boolean needSeparator = false;

        for (ImportOrderGroup importOrderGroup : groups) {
            if (importOrderGroup.isEmpty()) {
                continue;
            }

            // For configured groups, sorting is deferred; for fallback already sorted, but sorting again is harmless.
            importOrderGroup.sortByFqn();

            if (needSeparator) {
                sb.append('\n');
            }

            for (ImportDeclaration id : importOrderGroup.elements()) {
                sb.append(id.toString());
            }

            needSeparator = true;
        }

        return sb.toString();
    }

    /**
     *
     * @param javaFile
     * @param compilationUnit
     * @param workingDoc
     * @param rendered
     *     the newly rendered import block
     * @throws BadLocationException
     */
    private void replaceImportsInDocument(
        Path javaFile,
        CompilationUnit compilationUnit,
        IDocument workingDoc,
        String rendered
    )
        throws BadLocationException {
        @SuppressWarnings("unchecked")
        List<ImportDeclaration> imports = (List<ImportDeclaration>) compilationUnit.imports();

        if (imports.isEmpty()) {
            return;
        }

        int importStart = imports.getFirst().getStartPosition();
        int importEnd = imports
            .stream()
            .mapToInt(id -> ((ASTNode) id).getStartPosition() + ((ASTNode) id).getLength())
            .max()
            .orElse(importStart);

        workingDoc.replace(importStart, importEnd - importStart, rendered);
        try {
            TextEdit importRewrite = compilationUnit.rewrite(workingDoc, null);
            importRewrite.apply(workingDoc);
        } catch (IllegalArgumentException jdtEx) {
            String message = String.format(
                Locale.ROOT,
                "Problem formatting file [%s]. Range: [%d-%d]. Rendered old: >>>\n%s\n<<<",
                javaFile.toAbsolutePath(),
                importStart,
                importEnd,
                workingDoc.get().substring(importStart, importEnd)
            );

            throw new ImportOrderProcessorException(javaFile, message, jdtEx);
        }
    }

    // contains all imports read from the original source file.
    record Partition(List<ImportDeclaration> staticImports, List<ImportDeclaration> nonStatic) {}
}
