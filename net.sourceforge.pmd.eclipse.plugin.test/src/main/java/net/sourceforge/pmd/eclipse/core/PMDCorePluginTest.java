/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.core;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RulesetsFactoryUtils;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

/**
 * Test the PMD Core plugin
 * 
 * @author Philippe Herlin
 * 
 */
public class PMDCorePluginTest {

    /**
     * Test the default rulesets has been registered For this test to work, no Fragment or only the test plugin fragment
     * should be installed.
     * 
     */
    @Test
    public void testDefaultPMDRuleSetsRegistered() throws RuleSetNotFoundException {
        final Collection<RuleSet> defaultRuleSets = PMDPlugin.getDefault().getRuleSetManager().getRegisteredRuleSets();
        Assert.assertFalse("No registered default rulesets!", defaultRuleSets.isEmpty());

        final RuleSetFactory factory = RulesetsFactoryUtils.defaultFactory();
        final Iterator<RuleSet> iterator = factory.getRegisteredRuleSets();
        while (iterator.hasNext()) {
            final RuleSet ruleSet = iterator.next();
            Assert.assertTrue("RuleSet \"" + ruleSet.getName() + "\" has not been registered",
                    ruleSetRegistered(ruleSet, defaultRuleSets));
        }
    }

    /**
     * Test that the core plugin has been instantiated
     * 
     */
    @Test
    public void testPMDPluginNotNull() {
        Assert.assertNotNull("The Core Plugin has not been instantiated", PMDPlugin.getDefault());
    }

    /**
     * Test that we can get a ruleset manager
     * 
     */
    @Test
    public void testRuleSetManagerNotNull() {
        Assert.assertNotNull("Cannot get a ruleset manager", PMDPlugin.getDefault().getRuleSetManager());
    }

    /**
     * Test all the known PMD rulesets has been registered For this test to work, no fragment or only the test plugin
     * fragment should be installed.
     * 
     */
    @Test
    public void testStandardPMDRuleSetsRegistered() throws RuleSetNotFoundException {
        final Collection<RuleSet> registeredRuleSets = PMDPlugin.getDefault().getRuleSetManager()
                .getRegisteredRuleSets();
        Assert.assertFalse("No registered rulesets!", registeredRuleSets.isEmpty());

        final RuleSetFactory factory = RulesetsFactoryUtils.defaultFactory();
        final Iterator<RuleSet> iterator = factory.getRegisteredRuleSets();
        while (iterator.hasNext()) {
            final RuleSet ruleSet = iterator.next();
            Assert.assertTrue("RuleSet \"" + ruleSet.getName() + "\" has not been registered",
                    ruleSetRegistered(ruleSet, registeredRuleSets));
        }
    }

    /**
     * test if a ruleset is registered
     * 
     * @param ruleSet
     * @param set
     * @return true if OK
     */
    private boolean ruleSetRegistered(final RuleSet ruleSet, final Collection<RuleSet> set) {
        boolean registered = false;

        final Iterator<RuleSet> i = set.iterator();
        while (i.hasNext() && !registered) {
            final RuleSet registeredRuleSet = i.next();
            registered = registeredRuleSet.getName().equals(ruleSet.getName());
        }

        return registered;
    }
}
