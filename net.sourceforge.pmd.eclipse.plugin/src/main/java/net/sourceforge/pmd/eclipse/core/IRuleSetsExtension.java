/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.core;

import java.util.Set;

import net.sourceforge.pmd.lang.rule.RuleSet;

/**
 * This is the interface for implementors of the RuleSets extension point.
 *
 * @author Herlin
 *
 */

public interface IRuleSetsExtension {

    /**
     * Allows an extension to add more rules to to completely replace the sets of rulesets.
     * @param registeredRuleSets the already registered rulesets (modifiable set)
     */
    void registerRuleSets(Set<RuleSet> registeredRuleSets);

    /**
     * Allows an extension to specify rulesets that has to be loaded when no rulesets has been defined
     * for the plugin (for instance, after creating a new workspace).
     * @param defaultRuleSets the set of default rulesets (modifiable set)
     */
    void registerDefaultRuleSets(Set<RuleSet> defaultRuleSets);
}
