/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.eclipse.core.IRuleSetManager;

/**
 *
 *
 * @author Philippe Herlin
 *
 */
public class RuleSetManagerImpl implements IRuleSetManager {
    
    private final List<RuleSet> ruleSets = new ArrayList<RuleSet>();
    private final List<RuleSet> defaultRuleSets = new ArrayList<RuleSet>();

    /**
     * @see net.sourceforge.pmd.eclipse.core.IRuleSetManager#getRegisteredRuleSets()
     */
    public Collection<RuleSet> getRegisteredRuleSets() {
        return ruleSets;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.core.IRuleSetManager#registerRuleSet(net.sourceforge.pmd.RuleSet)
     */
    public void registerRuleSet(RuleSet ruleSet) {
        checkForNull(ruleSet);
        if (!ruleSets.contains(ruleSet)) {
            ruleSets.add(ruleSet);
        }
    }

    /**
     * @see net.sourceforge.pmd.eclipse.core.IRuleSetManager#unregisterRuleSet(net.sourceforge.pmd.RuleSet)
     */
    public void unregisterRuleSet(RuleSet ruleSet) {
        checkForNull(ruleSet);

        ruleSets.remove(ruleSet);
    }

    /**
     * @see net.sourceforge.pmd.eclipse.core.IRuleSetManager#getDefaultRuleSets()
     */
    public Collection<RuleSet> getDefaultRuleSets() {
        return defaultRuleSets;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.core.IRuleSetManager#registerDefaultRuleSet(net.sourceforge.pmd.RuleSet)
     */
    public void registerDefaultRuleSet(RuleSet ruleSet) {
        checkForNull(ruleSet);

        if (!defaultRuleSets.contains(ruleSet)) {
            defaultRuleSets.add(ruleSet);
        }
    }

    /**
     * @see net.sourceforge.pmd.eclipse.core.IRuleSetManager#unregisterDefaultRuleSet(net.sourceforge.pmd.RuleSet)
     */
    public void unregisterDefaultRuleSet(RuleSet ruleSet) {
        checkForNull(ruleSet);

        defaultRuleSets.remove(ruleSet);
    }
    
    private void checkForNull(RuleSet ruleSet) {
        if (ruleSet == null) {
            throw new IllegalArgumentException("ruleSet cannot be null"); // TODO NLS
        }
    }
}
