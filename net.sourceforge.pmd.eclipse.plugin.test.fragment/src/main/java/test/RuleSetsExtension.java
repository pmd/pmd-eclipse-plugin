/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package test;

import java.util.Set;

import org.eclipse.core.runtime.IStatus;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RulesetsFactoryUtils;
import net.sourceforge.pmd.eclipse.core.IRuleSetsExtension;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

/**
 * Sample of an RuleSets extension.
 * This will automatically registers our fragment rulesets into the core plugin.
 *
 * @author Herlin
 *
 */

public class RuleSetsExtension implements IRuleSetsExtension {
    private RuleSet ruleSet1;
    private RuleSet ruleSet2;

    /**
     * Replace the core plugin fragment with our own rulesets
     * @see net.sourceforge.pmd.eclipse.core.IRuleSetsExtension#registerRuleSets(java.util.Set)
     */
    @Override
    public void registerRuleSets(Set<RuleSet> registeredRuleSets) {
        try {
            RuleSet ruleSet1 = getRuleSet1();
            RuleSet ruleSet2 = getRuleSet2();

            // registeredRuleSets.clear(); // to remove all rulesets already registered
            registeredRuleSets.add(ruleSet1);
            registeredRuleSets.add(ruleSet2);
        } catch (RuleSetNotFoundException e) {
            PMDPlugin.getDefault().log(IStatus.ERROR, "Unable to load rulesets", e);
        }
    }

    /**
     * Replace the default rule sets. These rule sets are the one loaded if no rule sets has been configured yet
     * (for instance when creating a new workspace)
     * @see net.sourceforge.pmd.eclipse.core.IRuleSetsExtension#registerDefaultRuleSets(java.util.Set)
     */
    @Override
    public void registerDefaultRuleSets(Set<RuleSet> defaultRuleSets) {
        try {
            RuleSet ruleSet1 = getRuleSet1();
            RuleSet ruleSet2 = getRuleSet2();

            // registeredRuleSets.clear(); // to remove all rulesets already registered
            defaultRuleSets.add(ruleSet1);
            defaultRuleSets.add(ruleSet2);
        } catch (RuleSetNotFoundException e) {
            PMDPlugin.getDefault().log(IStatus.ERROR, "Unable to load rulesets", e);
        }
    }

    /**
     * Load the 1st ruleset
     * @return the 1st ruleset
     * @throws RuleSetNotFoundException
     */
    private RuleSet getRuleSet1() throws RuleSetNotFoundException {
        if (this.ruleSet1 == null) {
            RuleSetFactory factory = RulesetsFactoryUtils.defaultFactory();
            this.ruleSet1 = factory.createRuleSets("rulesets/extra1.xml").getAllRuleSets()[0];
        }

        return this.ruleSet1;
    }

    /**
     * Load the 2nd ruleset
     * @return the 2nd ruleset
     * @throws RuleSetNotFoundException
     */
    private RuleSet getRuleSet2() throws RuleSetNotFoundException {
        if (this.ruleSet2 == null) {
            RuleSetFactory factory = RulesetsFactoryUtils.defaultFactory();
            this.ruleSet2 = factory.createRuleSets("rulesets/extra2.xml").getAllRuleSets()[0];
        }

        return this.ruleSet2;
    }

}
