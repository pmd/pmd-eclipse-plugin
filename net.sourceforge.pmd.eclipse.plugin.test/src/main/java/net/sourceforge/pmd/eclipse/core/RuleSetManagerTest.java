/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.core;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.core.impl.RuleSetManagerImpl;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;
import net.sourceforge.pmd.lang.rule.RuleSet;

/**
 * Test the ruleset manager.
 * 
 * @author Philippe Herlin
 * 
 */
public class RuleSetManagerTest {
    private IRuleSetManager ruleSetManager;

    private static RuleSet createEmptyTestRuleSet() {
        return RuleSetUtil.newEmpty("test-ruleset", "Ruleset for unit testing");
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        this.ruleSetManager = new RuleSetManagerImpl();
    }

    /**
     * Registering twice the same rule set results in no addition
     * 
     */
    @Test
    public void testDuplicateRegister() {
        final RuleSet ruleSet = createEmptyTestRuleSet();
        this.ruleSetManager.registerRuleSet(ruleSet);
        this.ruleSetManager.registerRuleSet(ruleSet);
        Assert.assertEquals("Only one rule set should have been registered", 1,
                this.ruleSetManager.getRegisteredRuleSets().size());
    }

    /**
     * Registering twice the same default rule set results in no addition
     * 
     */
    @Test
    public void testDuplicateRegisterDefault() {
        final RuleSet ruleSet = createEmptyTestRuleSet();
        this.ruleSetManager.registerDefaultRuleSet(ruleSet);
        this.ruleSetManager.registerDefaultRuleSet(ruleSet);
        Assert.assertEquals("Only one rule set should have been registered", 1,
                this.ruleSetManager.getDefaultRuleSets().size());
    }

    /**
     * Unregistering twice the same rule set has no effect
     * 
     */
    @Test
    public void testDuplicateUnregister() {
        final RuleSet ruleSet = createEmptyTestRuleSet();
        this.ruleSetManager.registerRuleSet(ruleSet);

        this.ruleSetManager.unregisterRuleSet(ruleSet);
        this.ruleSetManager.unregisterRuleSet(ruleSet);
        Assert.assertEquals("RuleSet not unregistered", 0, this.ruleSetManager.getRegisteredRuleSets().size());
    }

    /**
     * Unregistering twice the same Default rule set has no effect.
     */
    @Test
    public void testDuplicateUnregisterDefault() {
        final RuleSet ruleSet = createEmptyTestRuleSet();
        this.ruleSetManager.registerRuleSet(ruleSet);

        this.ruleSetManager.unregisterDefaultRuleSet(ruleSet);
        this.ruleSetManager.unregisterDefaultRuleSet(ruleSet);
        Assert.assertEquals("Default RuleSet not unregistered", 0, this.ruleSetManager.getDefaultRuleSets().size());
    }

    /**
     * Test the register default ruleset.
     */
    @Test
    public void testRegisterDefaultRuleSet() {
        final RuleSet ruleSet = createEmptyTestRuleSet();
        this.ruleSetManager.registerDefaultRuleSet(ruleSet);
        Assert.assertEquals("Default RuleSet not registrered!", 1, this.ruleSetManager.getDefaultRuleSets().size());
    }

    /**
     * Test the registration of a null ruleset.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterNullDefaultRuleSet() {
        this.ruleSetManager.registerDefaultRuleSet(null);
    }

    /**
     * Test the registration of a null ruleset.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterNullRuleSet() {
        this.ruleSetManager.registerRuleSet(null);
    }

    /**
     * Test the register ruleset.
     */
    @Test
    public void testRegisterRuleSet() {
        final RuleSet ruleSet = createEmptyTestRuleSet();
        this.ruleSetManager.registerRuleSet(ruleSet);
        Assert.assertEquals("RuleSet not registrered!", 1, this.ruleSetManager.getRegisteredRuleSets().size());
    }

    /**
     * Test unregistration default.
     */
    @Test
    public void testUnregisterDefaultRuleSet() {
        final RuleSet ruleSet = createEmptyTestRuleSet();
        this.ruleSetManager.registerDefaultRuleSet(ruleSet);
        Assert.assertEquals("Default RuleSet not registered!", 1, this.ruleSetManager.getDefaultRuleSets().size());

        this.ruleSetManager.unregisterDefaultRuleSet(ruleSet);
        Assert.assertEquals("Default RuleSet not unregistered", 0, this.ruleSetManager.getDefaultRuleSets().size());
    }

    /**
     * Unregistering a null default ruleset is illegal.
     */
    @Test(expected = RuntimeException.class)
    public void testUnregisterNullDefaultRuleSet() {
        this.ruleSetManager.unregisterDefaultRuleSet(null);
    }

    /**
     * Unregistering a null ruleset is illegal.
     */
    @Test(expected = RuntimeException.class)
    public void testUnregisterNullRuleSet() {
        this.ruleSetManager.unregisterRuleSet(null);
    }

    /**
     * Test unregistration.
     */
    @Test
    public void testUnregisterRuleSet() {
        final RuleSet ruleSet = createEmptyTestRuleSet();
        this.ruleSetManager.registerRuleSet(ruleSet);
        Assert.assertEquals("RuleSet not registered!", 1, this.ruleSetManager.getRegisteredRuleSets().size());

        this.ruleSetManager.unregisterRuleSet(ruleSet);
        Assert.assertEquals("RuleSet not unregistered", 0, this.ruleSetManager.getRegisteredRuleSets().size());
    }
}
