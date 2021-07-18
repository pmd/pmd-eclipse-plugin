/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.properties.StringProperty;

/**
 * Holds a collection of rules as assembled by the tree widget manager.
 *
 * @author Brian Remedios
 */
public class RuleGroup<T extends Comparable<T>> implements RuleCollection, Comparable<RuleGroup<T>> {

    private T id;
    private String label;
    private String description;
    private List<Rule> rules = new ArrayList<>();

    public RuleGroup(T theId, String theLabel, String theDescription) {
        id = theId;
        label = theLabel;
        description = theDescription;
    }

    @Override
    public boolean isEmpty() {
        return rules == null || rules.isEmpty();
    }

    /**
     * If the receiver holds just a single rule then return it, otherwise return
     * null.
     *
     * @return Rule
     */
    @Override
    public Rule soleRule() {
        return rules.size() == 1 ? rules.get(0) : null;
    }

    @Override
    public boolean rulesDo(RuleVisitor visitor) {
        for (Rule rule : rules) {
            if (!visitor.accept(rule)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @return Comparable
     */
    public T id() {
        return id;
    }

    public String description() {
        return description;
    }

    public String label() {

        if (label != null) {
            return label;
        }
        return id == null ? "" : id.toString();
    }

    public void sortBy(Comparator<Rule> ruleComparator) {
        if (!hasRules()) {
            return;
        }

        Collections.sort(rules, ruleComparator);
    }

    public int ruleCount() {
        return rules.size();
    }

    public void add(Rule ref) {
        rules.add(ref);
    }

    public Rule[] rules() {
        return rules.toArray(new Rule[0]);
    }

    /**
     * Returns the value of the string property of all rules held by the
     * receiver, returns null if the values differ.
     *
     *
     * @return String
     */
    // TODO make this into a Generic method
    public String commonStringProperty(StringProperty desc) {

        if (rules.isEmpty()) {
            return null;
        }

        String value = rules.get(0).getProperty(desc);
        for (int i = 1; i < rules.size(); i++) {
            if (!StringUtils.equals(StringUtils.stripToNull(rules.get(i).getProperty(desc)),
                    StringUtils.stripToNull(value))) {
                return null;
            }
        }
        return value;
    }

    public boolean hasRules() {
        return !rules.isEmpty();
    }

    @Override
    public String toString() {
        return label() + " rules: " + ruleCount();
    }

    @Override
    public int compareTo(RuleGroup<T> otherGroup) {
        if (id == null) {
            return -1;
        }
        if (otherGroup.id() == null) {
            return -1;
        }

        return id.compareTo(otherGroup.id());
    }
}
