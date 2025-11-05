package io.github.bmarwell.jfmt.format;

import java.util.List;
import java.util.StringJoiner;
import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Thrown when a Java file contains syntax errors that prevent formatting.
 */
public class InvalidSyntaxException extends Exception {

    private final List<IProblem> problems;

    public InvalidSyntaxException(String message, IProblem[] problems) {
        super(message);
        this.problems = java.util.List.of(problems);
    }

    public List<IProblem> getProblems() {
        return problems;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InvalidSyntaxException.class.getSimpleName() + "[", "]")
            .add("super=" + super.toString())
            .add("problems=" + problems)
            .toString();
    }
}
