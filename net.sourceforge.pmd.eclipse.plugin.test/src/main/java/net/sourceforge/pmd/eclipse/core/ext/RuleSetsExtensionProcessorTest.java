/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.core.ext;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

/**
 * Test the ruleset extension
 * 
 * @author Philippe Herlin
 * 
 */
public class RuleSetsExtensionProcessorTest {

    /**
     * Tests the additional default rulesets has been registered. For this test to work, the test plugin fragment must
     * be installed.
     * 
     */
    @Test
    public void testAdditionalDefaultRuleSetsRegistered() throws RuleSetNotFoundException {
        final Collection<RuleSet> registeredRuleSets = PMDPlugin.getDefault().getRuleSetManager().getDefaultRuleSets();
        Assert.assertFalse("No registered default rulesets!", registeredRuleSets.isEmpty());

        final RuleSetFactory factory = new RuleSetFactory();
        RuleSet ruleSet = factory.createRuleSet("rulesets/extra1.xml");
        Assert.assertTrue("RuleSet \"rulesets/extra1.xml\" has not been registered",
                ruleSetRegistered(ruleSet, registeredRuleSets));

        ruleSet = factory.createRuleSet("rulesets/extra2.xml");
        Assert.assertTrue("RuleSet \"rulesets/extra2.xml\" has not been registered",
                ruleSetRegistered(ruleSet, registeredRuleSets));
    }

    /**
     * Tests the additional rulesets has been registered. For this test to work, the test plugin fragment must be
     * installed.
     * 
     */
    @Test
    public void testAdditionalRuleSetsRegistered() throws RuleSetNotFoundException {
        final Collection<RuleSet> registeredRuleSets = PMDPlugin.getDefault().getRuleSetManager()
                .getRegisteredRuleSets();
        Assert.assertFalse("No registered rulesets!", registeredRuleSets.isEmpty());

        final RuleSetFactory factory = new RuleSetFactory();
        RuleSet ruleSet = factory.createRuleSet("rulesets/extra1.xml");
        Assert.assertTrue("RuleSet \"rulesets/extra1.xml\" has not been registered",
                ruleSetRegistered(ruleSet, registeredRuleSets));

        ruleSet = factory.createRuleSet("rulesets/extra2.xml");
        Assert.assertTrue("RuleSet \"rulesets/extra2.xml\" has not been registered",
                ruleSetRegistered(ruleSet, registeredRuleSets));
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
