/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.quickfix;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

/**
 * Sample implementation of a fix that delete the line where the violation occurs.
 * 
 * @author Philippe Herlin
 *
 */
public class DeleteLineFix extends AbstractFix {

    public DeleteLineFix() {
        super("Delete the line");
    }

    @Override
    public String fix(String sourceCode, int lineNumber) {
        final Document document = new Document(sourceCode);
        try {
            final int offset = document.getLineOffset(lineNumber - 1);
            final int length = document.getLineLength(lineNumber - 1);
            document.replace(offset, length, "");
        } catch (BadLocationException e) { // NOPMD by Herlin on 11/10/06 00:20
            // ignoring that exception
        }

        return document.get();
    }
}
