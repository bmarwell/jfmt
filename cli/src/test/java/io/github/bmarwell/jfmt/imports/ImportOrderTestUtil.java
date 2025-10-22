package io.github.bmarwell.jfmt.imports;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Utility class for common test operations related to import order processing.
 * 
 * <p>Consider creating a JUnit extension for these utilities in the future to provide
 * better integration with the test lifecycle and reduce boilerplate code.
 */
final class ImportOrderTestUtil {

    private ImportOrderTestUtil() {
        // Utility class
    }

    /**
     * Loads a test resource file as a string.
     *
     * @param resourcePath
     *     the resource path relative to the classpath
     * @return the file contents as a string
     * @throws IllegalStateException
     *     if the resource cannot be loaded
     */
    static String loadTestResource(String resourcePath) {
        try (InputStream in = ImportOrderTestUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Test resource not found: " + resourcePath);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load test resource: " + resourcePath, e);
        }
    }

    /**
     * Parses Java source code into a CompilationUnit.
     *
     * @param sourceCode
     *     the Java source code to parse
     * @param unitName
     *     the name of the compilation unit
     * @return a parsed CompilationUnit with modifications recorded
     */
    static CompilationUnit parseCompilationUnit(String sourceCode, String unitName) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(sourceCode.toCharArray());
        parser.setUnitName(unitName);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_SOURCE, String.valueOf(AST.getJLSLatest()));
        options.put(JavaCore.COMPILER_COMPLIANCE, String.valueOf(AST.getJLSLatest()));
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, String.valueOf(AST.getJLSLatest()));
        parser.setCompilerOptions(options);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        cu.recordModifications();

        return cu;
    }

    /**
     * Finds the line number of the first static import in the given lines.
     *
     * @param lines
     *     the lines to search
     * @return the line number of the first static import, or -1 if not found
     */
    static int findFirstStaticImportLine(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith("import static ")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the line number of the first non-static import in the given lines.
     *
     * @param lines
     *     the lines to search
     * @return the line number of the first non-static import, or -1 if not found
     */
    static int findFirstNonStaticImportLine(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.startsWith("import ") && !trimmed.startsWith("import static ")) {
                return i;
            }
        }
        return -1;
    }
}
