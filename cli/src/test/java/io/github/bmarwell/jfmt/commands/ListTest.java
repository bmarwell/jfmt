package io.github.bmarwell.jfmt.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ListTest extends AbstractCommandTest {

    private static String pathToMixedImports() {
        return Path.of("target", "test-classes", "imports", "MixedImports.java").toString();
    }

    private static String pathToStaticImportsAtEnd() {
        return Path.of("target", "test-classes", "imports", "StaticImportsAtEnd.java").toString();
    }

    /**
     * Helper method to count occurrences of a search string within text.
     *
     * @param text
     *     the text to search in
     * @param searchString
     *     the string to search for
     * @return the number of occurrences found
     */
    private static int countOccurrences(String text, String searchString) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(searchString, index)) != -1) {
            count++;
            index += searchString.length();
        }
        return count;
    }

    @Test
    void reports_all_incorrectly_formatted_files_by_default() {
        // given - two files with formatting issues
        var args = new String[] { "list", pathToMixedImports(), pathToStaticImportsAtEnd() };

        // when
        var result = doExecute(args);

        // then
        assertEquals(1, result.returncode(), "Should return error code 1 when files have formatting issues");

        String stdout = String.join(System.lineSeparator(), result.stdout());
        assertTrue(
            stdout.contains("MixedImports.java"),
            "stdout should contain first file name but was: " + stdout
        );
        assertTrue(
            stdout.contains("StaticImportsAtEnd.java"),
            "stdout should contain second file name but was: " + stdout
        );
    }

    @Test
    void stops_at_first_incorrectly_formatted_file_with_no_all_flag() {
        // given - two files with formatting issues
        var args = new String[] { "list", "--no-all", pathToMixedImports(), pathToStaticImportsAtEnd() };

        // when
        var result = doExecute(args);

        // then
        assertEquals(1, result.returncode(), "Should return error code 1");

        String stdout = String.join(System.lineSeparator(), result.stdout());
        assertTrue(
            stdout.contains("MixedImports.java") || stdout.contains("StaticImportsAtEnd.java"),
            "stdout should contain at least one file name but was: " + stdout
        );

        // Verify only ONE filename is printed (fail-fast)
        long fileCount = stdout.lines()
            .filter(line -> line.contains(".java"))
            .count();
        assertEquals(1, fileCount, "With --no-all, only first error should be reported, but got " + fileCount);
    }

    @Test
    void filenames_output_to_stdout() {
        // given
        var args = new String[] { "list", pathToMixedImports() };

        // when
        var result = doExecute(args);

        // then
        assertEquals(1, result.returncode());

        String stdout = String.join(System.lineSeparator(), result.stdout());

        assertTrue(
            stdout.contains("MixedImports.java"),
            "stdout should contain filename for machine-readable output"
        );
    }

    @Test
    void file_not_reported_twice_with_all_flag() {
        // given - single file with formatting issues (default is all files mode)
        var args = new String[] { "list", pathToMixedImports() };

        // when
        var result = doExecute(args);

        // then
        assertEquals(1, result.returncode());

        String stdout = String.join(System.lineSeparator(), result.stdout());

        // Count occurrences of the filename
        int count = countOccurrences(stdout, "MixedImports.java");

        assertEquals(
            1,
            count,
            "File should be reported exactly once, not " + count + " times. stdout:\n" + stdout
        );
    }

    @Test
    void file_not_reported_twice_with_no_all_flag() {
        // given - single file with formatting issues
        var args = new String[] { "list", "--no-all", pathToMixedImports() };

        // when
        var result = doExecute(args);

        // then
        assertEquals(1, result.returncode());

        String stdout = String.join(System.lineSeparator(), result.stdout());

        // Count occurrences of the filename
        int count = countOccurrences(stdout, "MixedImports.java");

        assertEquals(
            1,
            count,
            "File should be reported exactly once, not " + count + " times. stdout:\n" + stdout
        );
    }

    @Test
    void multiple_files_each_reported_once_with_all_flag() {
        // given - two files with formatting issues (default is --all mode)
        var args = new String[] { "list", pathToMixedImports(), pathToStaticImportsAtEnd() };

        // when
        var result = doExecute(args);

        // then
        assertEquals(1, result.returncode());

        String stdout = String.join(System.lineSeparator(), result.stdout());

        // Verify each file appears exactly once
        long mixedCount = stdout.lines()
            .filter(line -> line.contains("MixedImports.java"))
            .count();
        long staticCount = stdout.lines()
            .filter(line -> line.contains("StaticImportsAtEnd.java"))
            .count();

        assertEquals(
            1,
            mixedCount,
            "MixedImports.java should appear exactly once, not " + mixedCount + " times. stdout:\n" + stdout
        );
        assertEquals(
            1,
            staticCount,
            "StaticImportsAtEnd.java should appear exactly once, not " + staticCount + " times. stdout:\n" + stdout
        );
    }

    @Test
    void returns_error_when_no_java_files_found() {
        // given - only non-Java files
        var args = new String[] { "list", "pom.xml" };

        // when
        var result = doExecute(args);

        // then
        assertEquals(1, result.returncode(), "Should return error code 1 when no Java files found");

        String stderr = String.join(System.lineSeparator(), result.stderr());
        assertTrue(
            stderr.contains("No Java files found"),
            "stderr should contain error message but was: " + stderr
        );
        assertFalse(
            stderr.contains("io.github.bmarwell.jfmt"),
            "stderr should not contain stack trace but was: " + stderr
        );
    }

}
