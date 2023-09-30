/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.runtime.cmd.internal;

import net.sourceforge.pmd.cpd.Mark;

public class CpdMarkWithSourceCode {
    private final Mark mark;
    private final CharSequence sourceCode;

    public CpdMarkWithSourceCode(CpdResult result, Mark mark) {
        this.mark = mark;
        sourceCode = result.getSourceCodeSlices().get(this.mark);
    }

    public Mark getMark() {
        return mark;
    }

    public CharSequence getSourceCode() {
        return sourceCode;
    }
}
