package io.github.bmarwell.jfmt.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    private final List<String> importOrderTokens;

    public ImportOrderProcessor(List<String> importOrderTokens) {
        this.importOrderTokens = importOrderTokens;
    }

    /**
     * Rewrites the imports in the provided document according to the configured order.
     * If there are no imports, this method is a no-op.
     */
    public void rewriteImportsIfAny(CompilationUnit compilationUnit, IDocument workingDoc)
        throws BadLocationException {
        @SuppressWarnings("unchecked")
        List<ImportDeclaration> imports = new ArrayList<>((List<ImportDeclaration>) compilationUnit.imports());
        if (imports.isEmpty()) {
            return;
        }

        // Partition into static and non-static once.
        Partition p = partitionImports(imports);

        // Decide configured vs fallback.
        List<ImportOrderGroup> groups = buildGroupsFromConfig(this.importOrderTokens, p);

        String rendered = renderGroups(groups);
        replaceImportsInDocument(compilationUnit, workingDoc, rendered);
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

    private List<ImportOrderGroup> buildGroupsFromConfig(List<String> importOrder, Partition p) {
        List<ImportDeclaration> remainingNonStatic = new ArrayList<>(p.nonStatic);
        List<ImportOrderGroup> groups = new ArrayList<>();
        // empty token collects leftovers AFTER other tokens
        ImportOrderGroup othersGroup = null;
        boolean staticConfigured = false;

        for (String token : importOrder) {
            String t = "\\#".equals(token) ? "#" : token; // unescape '#'
            if ("#".equals(t)) {
                ImportOrderGroup g = new ImportOrderGroup("#");
                g.addAll(p.staticImports);
                groups.add(g);
                staticConfigured = true;
                continue;
            }
            if (t.isEmpty()) {
                othersGroup = new ImportOrderGroup("");
                groups.add(othersGroup);
                continue;
            }

            ImportOrderGroup g = new ImportOrderGroup(t);
            for (Iterator<ImportDeclaration> it = remainingNonStatic.iterator(); it.hasNext();) {
                ImportDeclaration id = it.next();
                String fqn = id.getName().getFullyQualifiedName();
                if (fqn.startsWith(t)) {
                    g.add(id);
                    it.remove();
                }
            }
            groups.add(g);
        }

        // Fill others group with remaining non-static imports (if configured), otherwise create a trailing one
        if (othersGroup != null) {
            othersGroup.addAll(remainingNonStatic);
        } else if (!remainingNonStatic.isEmpty()) {
            ImportOrderGroup trailingOthers = new ImportOrderGroup("");
            trailingOthers.addAll(remainingNonStatic);
            groups.add(trailingOthers);
        }

        // If no static token configured, but we have static imports, append them as last group
        if (!staticConfigured && !p.staticImports.isEmpty()) {
            ImportOrderGroup g = new ImportOrderGroup("#");
            g.addAll(p.staticImports);
            groups.add(g);
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

    private void replaceImportsInDocument(CompilationUnit compilationUnit, IDocument workingDoc, String rendered)
        throws BadLocationException {
        int importStart = ((ImportDeclaration) compilationUnit.imports().getFirst()).getStartPosition();
        int importEnd = compilationUnit.imports()
            .stream()
            .mapToInt(id -> ((ASTNode) id).getStartPosition() + ((ASTNode) id).getLength())
            .max()
            .orElse(importStart);

        workingDoc.replace(importStart, importEnd - importStart, rendered);
        TextEdit importRewrite = compilationUnit.rewrite(workingDoc, null);
        importRewrite.apply(workingDoc);
    }

    private record Partition(List<ImportDeclaration> staticImports, List<ImportDeclaration> nonStatic) {}
}
