/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import net.sourceforge.pmd.eclipse.util.Util;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.lang.rule.RuleSet;

/**
 * @author Brian Remedios
 */
public class RuleSetTreeItemProvider implements ITreeContentProvider {

    private RuleFieldAccessor fieldAccessor;
    private final String groupDescription;
    private final Comparator<Rule> comparator;
    private final Map<Object, RuleGroup> ruleGroups;

    /**
     * Constructor for RuleSetTreeItemProvider.
     * 
     * @param accessor
     *            RuleFieldAccessor
     * @param description
     *            String
     */
    public RuleSetTreeItemProvider(RuleFieldAccessor accessor, String description, Comparator<Rule> theComparator) {
        fieldAccessor = accessor;
        groupDescription = description;
        comparator = theComparator;
        ruleGroups = new HashMap<>();
    }

    public void accessor(RuleFieldAccessor accessor) {
        fieldAccessor = accessor;
    }

    private Object[] sort(Collection<Rule> ruleColl) {
        Rule[] rules = ruleColl.toArray(new Rule[0]);
        if (comparator == null) {
            return rules;
        }

        Arrays.sort(rules, comparator);
        return rules;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof RuleSet) {
            RuleSet ruleSet = (RuleSet) parentElement;
            return fieldAccessor == null ? sort(ruleSet.getRules()) : asRuleGroups(ruleSet.getRules());
        }

        if (parentElement instanceof RuleGroup) {
            RuleGroup ruleGroup = (RuleGroup) parentElement;
            return ruleGroup.rules();
        }

        return Util.EMPTY_ARRAY;
    }

    private RuleGroup[] asRuleGroups(Collection<Rule> rules) {
        Iterator<Rule> iter = rules.iterator();
        ruleGroups.clear();

        while (iter.hasNext()) {
            Rule rule = iter.next();

            Comparable<?> groupId = fieldAccessor.valueFor(rule);

            RuleGroup group = ruleGroups.get(groupId);
            if (group != null) {
                group.add(rule);
            } else {
                group = new RuleGroup(groupId, fieldAccessor.labelFor(rule), groupDescription);
                group.add(rule);
                ruleGroups.put(groupId, group);
            }
        }

        RuleGroup[] groups = ruleGroups.values().toArray(new RuleGroup[0]);

        // TODO sort within groups
        for (RuleGroup group : groups) {
            group.sortBy(comparator);
        }

        Arrays.sort(groups);
        return groups;
    }

    private RuleGroup groupFor(Rule rule) {

        if (fieldAccessor == null) {
            return null;
        }

        Comparable<?> groupId = fieldAccessor.valueFor(rule);
        return ruleGroups.get(groupId);
    }

    /**
     * Return the effective parent of the element if we can figure it out.
     *
     * @param element
     *            Object
     * @return Object
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
     */
    @Override
    public Object getParent(Object element) {

        if (element instanceof RuleGroup) {
            return null;
        }

        if (element instanceof Rule) {
            return groupFor((Rule) element);
        }

        return null;
    }

    /**
     * Return whether the element has kids depending on what kind of parent it
     * might be.
     * 
     * @param element
     *            Object
     * @return boolean
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
     */
    @Override
    public boolean hasChildren(Object element) {

        if (element instanceof RuleSet) {
            RuleSet ruleSet = (RuleSet) element;
            return !ruleSet.getRules().isEmpty();
        }

        return element instanceof RuleGroup && ((RuleGroup) element).hasRules();
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }
}
