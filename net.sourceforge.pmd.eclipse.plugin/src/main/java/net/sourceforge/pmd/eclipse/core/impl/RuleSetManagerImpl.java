/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.pmd.eclipse.core.IRuleSetManager;
import net.sourceforge.pmd.lang.rule.RuleSet;

/**
 *
 *
 * @author Philippe Herlin
 *
 */
public class RuleSetManagerImpl implements IRuleSetManager {
    
    private final List<RuleSet> ruleSets = new ArrayList<>();
    private final List<RuleSet> defaultRuleSets = new ArrayList<>();

    @Override
    public Collection<RuleSet> getRegisteredRuleSets() {
        return ruleSets;
    }

    @Override
    public void registerRuleSet(RuleSet ruleSet) {
        checkForNull(ruleSet);
        if (!ruleSets.contains(ruleSet)) {
            ruleSets.add(ruleSet);
        }
    }

    @Override
    public void unregisterRuleSet(RuleSet ruleSet) {
        checkForNull(ruleSet);

        ruleSets.remove(ruleSet);
    }

    @Override
    public Collection<RuleSet> getDefaultRuleSets() {
        return defaultRuleSets;
    }

    @Override
    public void registerDefaultRuleSet(RuleSet ruleSet) {
        checkForNull(ruleSet);

        if (!defaultRuleSets.contains(ruleSet)) {
            defaultRuleSets.add(ruleSet);
        }
    }

    @Override
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
