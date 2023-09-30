/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.runtime.cmd.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.pmd.cpd.CPDReport;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;

public class CpdResult {
    private List<Match> matches = new ArrayList<>();
    private Map<Mark, CharSequence> sourceCodeSlices = new HashMap<>();

    public CpdResult(CPDReport report) {
        matches.addAll(report.getMatches());
        for (Match match : matches) {
            for (Mark mark : match.getMarkSet()) {
                sourceCodeSlices.put(mark, report.getSourceCodeSlice(mark));
            }
        }
    }

    public List<Match> getMatches() {
        return matches;
    }

    public Map<Mark, CharSequence> getSourceCodeSlices() {
        return sourceCodeSlices;
    }
}
