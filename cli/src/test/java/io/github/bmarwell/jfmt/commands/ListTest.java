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

    @Test
    void reports_all_incorrectly_formatted_files_by_default() {
        // given - two files with formatting issues
        var args = new String[] { "list", pathToMixedImports(), pathToStaticImportsAtEnd() };

        // when
        var result = doExecute(args);

        // then
        assertEquals(1, result.returncode(), "Should return error code 1 when files have formatting issues");

        String stderr = String.join(System.lineSeparator(), result.stderr());
        assertTrue(
            stderr.contains("MixedImports.java"),
            "stderr should contain first file name but was: " + stderr
        );
        assertTrue(
            stderr.contains("StaticImportsAtEnd.java"),
            "stderr should contain second file name but was: " + stderr
        );
        assertTrue(
            stderr.contains("Not formatted correctly"),
            "stderr should contain error message but was: " + stderr
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

        String stderr = String.join(System.lineSeparator(), result.stderr());
        assertTrue(
            stderr.contains("MixedImports.java") || stderr.contains("StaticImportsAtEnd.java"),
            "stderr should contain at least one file name but was: " + stderr
        );
        assertTrue(
            stderr.contains("Not formatted correctly"),
            "stderr should contain error message but was: " + stderr
        );

        // Verify only ONE error message is printed (fail-fast)
        long errorCount = stderr.lines()
            .filter(line -> line.contains("Not formatted correctly"))
            .count();
        assertEquals(1, errorCount, "With --no-all, only first error should be reported, but got " + errorCount);
    }

    @Test
    void error_output_goes_to_stderr_not_stdout() {
        // given
        var args = new String[] { "list", pathToMixedImports() };

        // when
        var result = doExecute(args);

        // then
        assertEquals(1, result.returncode());

        String stdout = String.join(System.lineSeparator(), result.stdout());
        String stderr = String.join(System.lineSeparator(), result.stderr());

        assertFalse(
            stdout.contains("Not formatted correctly"),
            "stdout should not contain error messages"
        );
        assertTrue(
            stderr.contains("Not formatted correctly"),
            "stderr should contain error messages"
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

        String stderr = String.join(System.lineSeparator(), result.stderr());

        // Count occurrences of the filename in error messages
        int count = 0;
        int index = 0;
        String searchString = "Not formatted correctly";
        while ((index = stderr.indexOf(searchString, index)) != -1) {
            count++;
            index += searchString.length();
        }

        assertEquals(
            1,
            count,
            "File should be reported exactly once, not " + count + " times. stderr:\n" + stderr
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

        String stderr = String.join(System.lineSeparator(), result.stderr());

        // Count occurrences of the filename in error messages
        int count = 0;
        int index = 0;
        String searchString = "Not formatted correctly";
        while ((index = stderr.indexOf(searchString, index)) != -1) {
            count++;
            index += searchString.length();
        }

        assertEquals(
            1,
            count,
            "File should be reported exactly once, not " + count + " times. stderr:\n" + stderr
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

        String stderr = String.join(System.lineSeparator(), result.stderr());

        // Count total error messages
        int count = 0;
        int index = 0;
        String searchString = "Not formatted correctly";
        while ((index = stderr.indexOf(searchString, index)) != -1) {
            count++;
            index += searchString.length();
        }

        assertEquals(
            2,
            count,
            "Each file should be reported exactly once (2 files = 2 messages), not " + count + " times. stderr:\n"
                + stderr
        );

        // Verify each file appears exactly once in "Not formatted correctly" messages
        String[] errorLines = stderr.lines()
            .filter(line -> line.contains("Not formatted correctly"))
            .toArray(String[]::new);

        long mixedErrorCount = java.util.Arrays.stream(errorLines)
            .filter(line -> line.contains("MixedImports.java"))
            .count();
        long staticErrorCount = java.util.Arrays.stream(errorLines)
            .filter(line -> line.contains("StaticImportsAtEnd.java"))
            .count();

        assertEquals(
            1,
            mixedErrorCount,
            "MixedImports.java should appear in error message exactly once, not " + mixedErrorCount + " times"
        );
        assertEquals(
            1,
            staticErrorCount,
            "StaticImportsAtEnd.java should appear in error message exactly once, not " + staticErrorCount + " times"
        );
    }

}
