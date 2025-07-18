/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.quickfix;

import org.eclipse.jface.text.Document;

/**
 * 
 * @author Brian Remedios
 *
 */
public class CommentOutLineFix extends AbstractFix {

    public CommentOutLineFix() {
        super("Comment out the line");
    }

    @Override
    public String fix(String sourceCode, int lineNumber) {
        final Document document = new Document(sourceCode);

        // TODO

        return document.get();
    }
}
