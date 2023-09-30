/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.runtime.cmd.internal;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;

public class CpdMatchWithSourceCode {
    private final Match match;
    private final Map<Mark, CharSequence> sourceCodeSlices = new HashMap<>();

    public CpdMatchWithSourceCode(CpdResult result, Match match) {
        this.match = match;
        for (Mark mark : this.match.getMarkSet()) {
            sourceCodeSlices.put(mark, result.getSourceCodeSlices().get(mark));
        }
    }

    public Match getMatch() {
        return match;
    }

    public Map<Mark, CharSequence> getSourceCodeSlices() {
        return sourceCodeSlices;
    }
}
