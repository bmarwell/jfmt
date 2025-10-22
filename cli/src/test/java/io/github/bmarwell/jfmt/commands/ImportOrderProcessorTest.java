package io.github.bmarwell.jfmt.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.bmarwell.jfmt.imports.ImportOrderConfiguration;
import io.github.bmarwell.jfmt.imports.ImportOrderLoader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ImportOrderProcessorTest {

    @AutoClose
    static InputStream in =
        new ByteArrayInputStream("1=\\#\n2=\n3=java|javax|jakarta".getBytes(StandardCharsets.UTF_8));

    static ImportOrderConfiguration configuration;

    private final ImportOrderProcessor processor = new ImportOrderProcessor(configuration);

    @BeforeAll
    static void loadSource() throws IOException {
        configuration = new ImportOrderLoader().readTokens(in);
    }

    @Test
    void creates_groups_correctly() {
        // given
        AST ast = AST.newAST(AST.getJLSLatest(), false);
        var staticIsRegularFile = ast.newImportDeclaration();
        staticIsRegularFile.setStatic(true);
        staticIsRegularFile.setName(ast.newName("java.io.File.isRegularFile"));

        List<ImportDeclaration> staticImports = List.of(
            staticIsRegularFile
        );

        var orgApacheShiroSecurityRealm = ast.newImportDeclaration();
        orgApacheShiroSecurityRealm.setStatic(false);
        orgApacheShiroSecurityRealm.setName(ast.newName("org.apache.shiro.realm.Realm"));

        var jakartaInjectInject = ast.newImportDeclaration();
        jakartaInjectInject.setStatic(false);
        jakartaInjectInject.setName(ast.newName("jakarta.inject.Inject"));

        var javaxInjectInject = ast.newImportDeclaration();
        javaxInjectInject.setStatic(false);
        javaxInjectInject.setName(ast.newName("javax.inject.Inject"));

        var javaUtilList = ast.newImportDeclaration();
        javaUtilList.setStatic(false);
        javaUtilList.setName(ast.newName("java.util.List"));

        List<ImportDeclaration> nonStatic = List.of(
            orgApacheShiroSecurityRealm,
            jakartaInjectInject,
            javaUtilList,
            javaxInjectInject
        );

        ImportOrderProcessor.Partition p = new ImportOrderProcessor.Partition(staticImports, nonStatic);

        // when
        List<ImportOrderGroup> importOrderGroups = processor.buildGroupsFromConfig(p);

        // then
        assertEquals(3, importOrderGroups.size());
        ImportOrderGroup importOrderGroup = importOrderGroups.get(2);
        assertEquals("java|javax|jakarta", importOrderGroup.name());
    }

}
