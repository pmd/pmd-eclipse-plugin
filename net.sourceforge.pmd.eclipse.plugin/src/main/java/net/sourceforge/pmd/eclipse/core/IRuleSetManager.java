/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.core;

import java.util.Collection;

import net.sourceforge.pmd.lang.rule.RuleSet;

/**
 * Interface for a rule set manager. A RuleSetManager handle a set of rule sets.
 *
 * @author Philippe Herlin
 *
 */
public interface IRuleSetManager {

    /**
     * Register an additional rule set.
     * @param ruleSet the ruleset to register
     */
    void registerRuleSet(RuleSet ruleSet);

    /**
     * Unregister a rule set.
     * @param ruleSet the ruleset to unregister
     */
    void unregisterRuleSet(RuleSet ruleSet);

    /**
     * Gets the additionally registered rulesets.
     * @return a set of registered ruleset; this can be empty but never null
     */
    Collection<RuleSet> getRegisteredRuleSets();

    /**
     * Register a rule set for the default set.
     * @param ruleSet the ruleset to register
     */
    void registerDefaultRuleSet(RuleSet ruleSet);

    /**
     * Unregister a rule set from the default set.
     * @param ruleSet the ruleset to unregister
     */
    void unregisterDefaultRuleSet(RuleSet ruleSet);

    /**
     * Gets the default rulesets.
     * @return the plugin default ruleset set
     */
    Collection<RuleSet> getDefaultRuleSets();
}
