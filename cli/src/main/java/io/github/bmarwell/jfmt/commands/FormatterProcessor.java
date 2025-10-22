package io.github.bmarwell.jfmt.commands;

import java.util.Objects;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

public class FormatterProcessor {

    private final CodeFormatter formatter;

    public FormatterProcessor(CodeFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * Formats the entire document according to the configured formatter.
     *
     * @param workingDoc
     *     the document to format
     * @throws BadLocationException
     *     if the text edits cannot be applied
     */
    public void formatDocument(IDocument workingDoc) throws BadLocationException {
        final TextEdit edit = formatter.format(
            CodeFormatter.K_COMPILATION_UNIT,
            workingDoc.get(),
            0,
            workingDoc.getLength(),
            0,
            "\n"
        );

        Objects.requireNonNull(edit, "Formatting edits must not be null.");

        edit.apply(workingDoc);
    }
}
