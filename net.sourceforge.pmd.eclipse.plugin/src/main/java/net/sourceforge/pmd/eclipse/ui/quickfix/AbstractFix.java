/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.quickfix;

public abstract class AbstractFix implements Fix {

    private final String label;

    protected AbstractFix(String theLabel) {
        label = theLabel;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
