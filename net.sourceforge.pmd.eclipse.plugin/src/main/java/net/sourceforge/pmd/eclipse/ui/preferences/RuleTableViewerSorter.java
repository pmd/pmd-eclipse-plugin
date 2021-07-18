/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences;

import java.util.Comparator;
import java.util.Locale;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import net.sourceforge.pmd.Rule;

/**
 * Sorter for the rule table in the PMD Preference page.
 * 
 * @author Herlin
 *
 */

public class RuleTableViewerSorter extends ViewerComparator {

    /**
     * Default Rule comparator for tabular display of Rules.
     */
    public static final Comparator<Rule> RULE_DEFAULT_COMPARATOR = new Comparator<Rule>() {
        @Override
        public int compare(Rule r1, Rule r2) {
            int cmp = RULE_RULESET_NAME_COMPARATOR.compare(r1, r2);
            if (cmp == 0) {
                cmp = RULE_NAME_COMPARATOR.compare(r1, r2);
            }
            return cmp;
        }
    };

    /**
     * Rule Language comparator for tabular display of Rules.
     */
    public static final Comparator<Rule> RULE_LANGUAGE_COMPARATOR = new Comparator<Rule>() {
        @Override
        public int compare(Rule r1, Rule r2) {
            return compareStrings(r1.getLanguage().getName(), r2.getLanguage().getName());
        }
    };

    /**
     * RuleSet Name comparator for tabular display of Rules.
     */
    public static final Comparator<Rule> RULE_RULESET_NAME_COMPARATOR = new Comparator<Rule>() {
        @Override
        public int compare(Rule r1, Rule r2) {
            return compareStrings(r1.getRuleSetName(), r2.getRuleSetName());
        }
    };

    /**
     * Rule Name comparator for tabular display of Rules.
     */
    public static final Comparator<Rule> RULE_NAME_COMPARATOR = new Comparator<Rule>() {
        @Override
        public int compare(Rule r1, Rule r2) {
            return compareStrings(r1.getName(), r2.getName());
        }
    };

    /**
     * Rule Since comparator for tabular display of Rules.
     */
    public static final Comparator<Rule> RULE_SINCE_COMPARATOR = new Comparator<Rule>() {
        @Override
        public int compare(Rule r1, Rule r2) {
            return compareStrings(r1.getSince(), r2.getSince());
        }
    };

    /**
     * Rule Priority comparator for tabular display of Rules.
     */
    public static final Comparator<Rule> RULE_PRIORITY_COMPARATOR = new Comparator<Rule>() {
        @Override
        public int compare(Rule r1, Rule r2) {
            return r1.getPriority().getPriority() - r2.getPriority().getPriority();
        }
    };

    /**
     * Rule Description comparator for tabular display of Rules.
     */
    public static final Comparator<Rule> RULE_DESCRIPTION_COMPARATOR = new Comparator<Rule>() {
        @Override
        public int compare(Rule r1, Rule r2) {
            return compareStrings(r1.getDescription(), r2.getDescription());
        }
    };

    private Comparator<Rule> comparator;
    private boolean sortDescending = false;

    /**
     * Constructor.
     * 
     * @param comparator
     *            the initial comparator
     */
    public RuleTableViewerSorter(Comparator<Rule> comparator) {
        this.comparator = comparator;
    }

    /**
     * @return Returns the sortDescending.
     */
    public boolean isSortDescending() {
        return sortDescending;
    }

    /**
     * @param sortDescending
     *            The sortDescending to set.
     */
    public void setSortDescending(boolean sortDescending) {
        this.sortDescending = sortDescending;
    }

    /**
     * Set a comparator. If the same comparator is already set, then change the
     * sorting order.
     * 
     * @param comparator
     *            The comparator to set.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void setComparator(Comparator<Rule> comparator) {
        if (this.comparator != comparator) {
            this.comparator = comparator;
        } else {
            this.sortDescending = !sortDescending;
        }
    }

    @Override
    protected Comparator<? super String> getComparator() {
        // safe-guard
        throw new UnsupportedOperationException("Getting the underlaying comparator is not allowed"
                + " - since a different one is actually used");
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        int result = comparator.compare((Rule) e1, (Rule) e2);
        return sortDescending ? 0 - result : result;
    }

    /**
     * Compare string pairs while handling nulls and trimming whitespace.
     * 
     * @param s1
     * @param s2
     * @return int
     */
    private static int compareStrings(String s1, String s2) {
        String str1 = s1 == null ? "" : s1.trim().toUpperCase(Locale.ROOT);
        String str2 = s2 == null ? "" : s2.trim().toUpperCase(Locale.ROOT);
        return str1.compareTo(str2);
    }
}
