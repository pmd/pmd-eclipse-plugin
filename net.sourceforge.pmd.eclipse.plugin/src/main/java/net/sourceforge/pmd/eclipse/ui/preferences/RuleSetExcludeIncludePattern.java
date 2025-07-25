/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.eclipse.ui.actions.internal.InternalRuleSetUtil;
import net.sourceforge.pmd.lang.rule.RuleSet;

/**
 * Helper class to display rule set exclude/include patterns in a table
 *
 */
public class RuleSetExcludeIncludePattern {
    private final RuleSet ruleSet;
    private final boolean exclude;
    private final int index;

    /**
     * Constructor with a RuleSet object and an index
     */
    public RuleSetExcludeIncludePattern(RuleSet ruleSet, boolean exclude, int index) {
        this.ruleSet = ruleSet;
        this.exclude = exclude;
        this.index = index;
    }

    /**
     * Returns the pattern.
     * 
     * @return String
     */
    public String getPattern() {
        List<String> patterns = getPatterns();
        return patterns.get(index);
    }

    /**
     * Sets the pattern.
     * 
     * @param value
     *            The value to set
     */
    public void setPattern(String value) {
        List<String> patterns = getPatterns();
        patterns.set(index, value);
    }

    private List<String> getPatterns() {
        return exclude
                ? new ArrayList<>(InternalRuleSetUtil.convert(ruleSet.getFileExclusions()))
                : new ArrayList<>(InternalRuleSetUtil.convert(ruleSet.getFileInclusions()));
    }
}
