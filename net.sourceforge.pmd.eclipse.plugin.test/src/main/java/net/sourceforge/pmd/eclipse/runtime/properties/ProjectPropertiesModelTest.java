/*
 * Created on 6 fevr. 2005
 * 
 * Copyright (c) 2004, PMD for Eclipse Development Team All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. * Redistributions
 * in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. * The end-user documentation
 * included with the redistribution, if any, must include the following
 * acknowledgement: "This product includes software developed in part by
 * support from the Defense Advanced Research Project Agency (DARPA)" *
 * Neither the name of "PMD for Eclipse Development Team" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.pmd.eclipse.runtime.properties;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkingSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.builder.PMDNature;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferencesManager;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;

/**
 * Test the project properties model.
 * 
 * @author Philippe Herlin
 * 
 */
public class ProjectPropertiesModelTest {
    private IProject testProject;
    private RuleSet initialPluginRuleSet;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {

        // 1. Create a Java project
        this.testProject = EclipseUtils.createJavaProject("PMDTestProject");
        Assert.assertTrue("A test project cannot be created; the tests cannot be performed.",
                this.testProject != null && this.testProject.exists() && this.testProject.isAccessible());

        // 2. Keep the plugin ruleset
        this.initialPluginRuleSet = PMDPlugin.getDefault().getPreferencesManager().getRuleSet();
        this.initialPluginRuleSet = RuleSetUtil.clearRules(this.initialPluginRuleSet);
        final Collection<RuleSet> defaultRuleSets = PMDPlugin.getDefault().getRuleSetManager().getDefaultRuleSets();
        Assert.assertEquals(0, this.initialPluginRuleSet.getRules().size());
        int ruleCount = 0;
        for (final RuleSet ruleSet : defaultRuleSets) {
            int countBefore = this.initialPluginRuleSet.getRules().size();
            int expectedCount = countBefore + ruleSet.getRules().size();
            ruleCount = ruleCount + ruleSet.getRules().size();
            this.initialPluginRuleSet = RuleSetUtil.addRules(this.initialPluginRuleSet, ruleSet.getRules());
            Assert.assertEquals(expectedCount, this.initialPluginRuleSet.getRules().size());
        }
        Assert.assertEquals(ruleCount, this.initialPluginRuleSet.getRules().size());
        RuleSet cloned = RuleSetUtil.newCopyOf(this.initialPluginRuleSet);
        Assert.assertEquals(cloned.getRules(), this.initialPluginRuleSet.getRules());

        PMDPlugin.getDefault().getPreferencesManager().setRuleSet(this.initialPluginRuleSet);

    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        // 1. Delete the test project
        if (this.testProject != null) {
            if (this.testProject.exists() && this.testProject.isAccessible()) {
                this.testProject.delete(true, true, null);
                this.testProject = null;
            }
        }

        // 2. Restore the plugin initial rule set
        PMDPlugin.getDefault().getPreferencesManager().setRuleSet(this.initialPluginRuleSet);
    }

    public static void compareTwoRuleSets(RuleSet ruleSet1, RuleSet ruleSet2) {
        if (!ruleSet1.getRules().equals(ruleSet2.getRules())) {
            System.out.println("###################################################");
            System.out.println("RuleSet1: " + ruleSet1 + " RuleSet2: " + ruleSet2);
            Iterator<Rule> it1 = ruleSet1.getRules().iterator();
            Iterator<Rule> it2 = ruleSet2.getRules().iterator();
            for (int i = 0; i < ruleSet2.getRules().size(); i++) {
                Rule pluginRule = it1.next();
                Rule projectRule = it2.next();

                if (pluginRule != projectRule) {
                    System.out.println("i=" + i + ": pluginRule=" + pluginRule + " projectRule=" + projectRule);
                    System.out.println("plugin: " + pluginRule.getName() + " (" + pluginRule.getLanguage() + ")");
                    System.out.println("project: " + projectRule.getName() + " (" + projectRule.getLanguage() + ")");
                }
            }
            System.out.println("###################################################");
        }
    }

    /**
     * Bug: when a user deselect a project rule it is not saved
     */
    @Test
    public void testBug() throws PropertiesException, RuleSetNotFoundException, CoreException {
        // First ensure that the plugin initial ruleset is equal to the project
        // ruleset
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        RuleSet projectRuleSet = model.getProjectRuleSet();
        Assert.assertEquals(this.initialPluginRuleSet.getRules().size(), projectRuleSet.getRules().size());
        compareTwoRuleSets(initialPluginRuleSet, projectRuleSet);
        Assert.assertEquals("The project ruleset is not equal to the plugin ruleset",
                this.initialPluginRuleSet.getRules(), projectRuleSet.getRules());
        int ruleCountBefore = projectRuleSet.getRules().size();

        // 2. remove a rule (keep its name for assertion)
        RuleSet newRuleSet = RuleSetUtil.newEmpty("test-ruleset", "RuleSet for unit testing");
        newRuleSet = RuleSetUtil.addRules(newRuleSet, projectRuleSet.getRules());
        final Rule removedRule = newRuleSet.getRuleByName("UnnecessaryParentheses");
        newRuleSet = RuleSetUtil.removeRule(newRuleSet, removedRule);
        Assert.assertEquals("No rule has been removed - test problem", newRuleSet.getRules().size(),
                ruleCountBefore - 1);

        model.setProjectRuleSet(newRuleSet);
        model.sync();

        // 3. test the rule has correctly been removed
        projectRuleSet = model.getProjectRuleSet();
        Assert.assertEquals("The rule count should 1 less", ruleCountBefore - 1, projectRuleSet.getRules().size());
        for (Rule r : projectRuleSet.getRules()) {
            if (r.getName().equals(removedRule.getName()) && r.getLanguage() == removedRule.getLanguage()) {
                Assert.fail("The rule has not been removed!");
            }
        }
    }

    /**
     * A property should be used to know id PMD is enabled for a project. Set to FALSE.
     * 
     */
    @Test
    public void testPmdEnabledFALSE() throws PropertiesException, CoreException {
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        model.setPmdEnabled(true);
        model.sync();
        Assert.assertTrue("Cannot activate PMD for that project", this.testProject.hasNature(PMDNature.PMD_NATURE));

        model.setPmdEnabled(false);
        model.sync();
        Assert.assertFalse("Cannot desactivate PMD for that project", this.testProject.hasNature(PMDNature.PMD_NATURE));
        Assert.assertFalse("PMD Property not reset!", model.isPmdEnabled());

    }

    /**
     * A property should be used to know if PMD is enabled for a project. Set to TRUE
     * 
     */
    @Test
    public void testPmdEnabledTRUE() throws CoreException, PropertiesException {
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        model.setPmdEnabled(true);
        model.sync();
        Assert.assertTrue("Cannot activate PMD for that project", this.testProject.hasNature(PMDNature.PMD_NATURE));
        Assert.assertTrue("PMD Property not set!", model.isPmdEnabled());
    }

    /**
     * A brand new project should be affected the Plugin ruleset in the global ruleset.
     * 
     */
    @Test
    public void testProjectRuleSet() throws PropertiesException {
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        final IPreferencesManager pmgr = PMDPlugin.getDefault().getPreferencesManager();
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(byteStream);

        Assert.assertTrue("A new project is not created with the default plugin ruleset", EclipseUtils
                .assertRuleSetEquals(model.getProjectRuleSet().getRules(), pmgr.getRuleSet().getRules(), out));
    }

    /**
     * Set another ruleset.
     */
    @Test
    public void testProjectRuleSet1() throws PropertiesException, RuleSetNotFoundException, CoreException {
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        final RuleSetFactory factory = new RuleSetFactory();

        // use the basic ruleset because it should be included in the plugin
        // ruleset.
        final RuleSet basicRuleSet = factory.createRuleSet("rulesets/java/basic.xml");

        // First set the project ruleset
        model.setProjectRuleSet(basicRuleSet);
        model.sync();

        // Test the ruleset we set is equal to the ruleset we queried
        final RuleSet projectRuleSet = model.getProjectRuleSet();
        Assert.assertNotNull("Project ruleset has not been set", projectRuleSet);
        Assert.assertTrue("The project ruleset is not the basic ruleset",
                EclipseUtils.assertRuleSetEquals(basicRuleSet.getRules(), projectRuleSet.getRules(), System.out));
    }

    /**
     * When rules are removed from the plugin preferences, these rules should also be removed from the project euh...
     * ben en fait non. annulé.
     */
    @Test
    public void testProjectRuleSet2() throws PropertiesException, RuleSetNotFoundException, CoreException {
        /*
         * // First ensure that the plugin initial ruleset is equal to the project // ruleset IProjectPropertiesManager
         * mgr = PMDPlugin.getDefault().getPropertiesManager(); IProjectProperties model =
         * mgr.loadProjectProperties(this.testProject);
         * 
         * RuleSet projectRuleSet = model.getProjectRuleSet();
         * assertEquals("The project ruleset is not equal to the plugin ruleset", this.initialPluginRuleSet.getRules(),
         * projectRuleSet .getRules());
         * 
         * // use the basic ruleset and set it at the only plugin ruleset RuleSetFactory factory = new RuleSetFactory();
         * RuleSet basicRuleSet = factory.createSingleRuleSet("rulesets/basic.xml");
         * 
         * IPreferencesManager pmgr = PMDPlugin.getDefault().getPreferencesManager(); pmgr.setRuleSet(basicRuleSet);
         * 
         * projectRuleSet = model.getProjectRuleSet();
         * 
         * dumpRuleSet(basicRuleSet); dumpRuleSet(projectRuleSet);
         * assertEquals("The project ruleset is not equal to the plugin ruleset", basicRuleSet.getRules(),
         * projectRuleSet.getRules());
         */
    }

    /**
     * When rules are added to the plugin preferences, these rules should also be added to the project
     */
    @Test
    public void testProjectRuleSet3() throws PropertiesException, RuleSetNotFoundException, CoreException {

        // First ensure that the plugin initial ruleset is equal to the project
        // ruleset
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        RuleSet projectRuleSet = model.getProjectRuleSet();
        Assert.assertEquals("The project ruleset is not equal to the plugin ruleset",
                this.initialPluginRuleSet.getRules(), projectRuleSet.getRules());

        // 2. add a rule to the plugin rule set
        final Rule myRule = new AbstractJavaRule() {
            @Override
            public String getName() {
                return "MyRule";
            }
        };

        RuleSet newRuleSet = RuleSetUtil.newEmpty("foo", "bar");
        newRuleSet = RuleSetUtil.addRules(newRuleSet, this.initialPluginRuleSet.getRules());
        newRuleSet = RuleSetUtil.addRule(newRuleSet, myRule);
        PMDPlugin.getDefault().getPreferencesManager().setRuleSet(newRuleSet);

        // Test that the project rule set should still be the same as the plugin
        // rule set
        model = mgr.loadProjectProperties(this.testProject);
        projectRuleSet = model.getProjectRuleSet();
        Assert.assertEquals("The project ruleset is not equal to the plugin ruleset",
                PMDPlugin.getDefault().getPreferencesManager().getRuleSet().getRules(), projectRuleSet.getRules());
    }

    /**
     * It should not be possible to set to null a project ruleset
     * 
     */
    @Test
    public void testProjectRuleSetNull() throws PropertiesException {
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        try {
            model.setProjectRuleSet(null);
            Assert.fail("A ModelException must be raised when setting a project ruleset to null");
        } catch (final PropertiesException e) {
            // OK that's correct
        }

    }

    /**
     * A project may work only on a subset of files defined by a working set
     * 
     */
    @Test
    public void testProjectWorkingSetNull() throws PropertiesException {
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        model.setProjectWorkingSet(null);
        final IWorkingSet w = model.getProjectWorkingSet();
        Assert.assertNull("The project should not have a working set defined", w);
    }

    /**
     * A project may know if it should be rebuilt or not
     * 
     */
    @Test
    public void testRebuild1() throws PropertiesException {
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        model.setPmdEnabled(false);
        model.setProjectWorkingSet(null);
        model.setRuleSetStoredInProject(false);
        model.setNeedRebuild(false);
        Assert.assertFalse(model.isNeedRebuild());
    }

    /**
     * A project may know if it should be rebuilt or not
     * 
     */
    @Test
    public void testRebuild2() throws PropertiesException {
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        model.setPmdEnabled(true);
        Assert.assertTrue(model.isNeedRebuild());
    }

    /**
     * A project may know if it should be rebuilt or not
     * 
     */
    @Test
    public void testRebuild3() throws PropertiesException {
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        model.setPmdEnabled(true);

        final RuleSet pmdRuleSet = PMDPlugin.getDefault().getPreferencesManager().getRuleSet();
        final Rule rule1 = pmdRuleSet.getRuleByName("EmptyCatchBlock");
        final RuleSet fooRuleSet = RuleSetUtil.newSingle(rule1);

        model.setProjectRuleSet(fooRuleSet);
        Assert.assertTrue(model.isNeedRebuild());
    }

    /**
     * A project may have its ruleset stored in the project own directory. Test set to FALSE.
     * 
     */
    @Test
    public void testRuleSetStoredInProjectFALSE() throws PropertiesException, RuleSetNotFoundException {
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        final RuleSetFactory factory = new RuleSetFactory();
        final RuleSet basicRuleSet = factory.createRuleSet("rulesets/java/basic.xml");
        model.setPmdEnabled(true);
        model.setRuleSetStoredInProject(false);
        model.setProjectWorkingSet(null);
        model.setProjectRuleSet(basicRuleSet);
        model.sync();

        model.createDefaultRuleSetFile();
        model.setRuleSetStoredInProject(true);
        model.sync();

        model.setRuleSetStoredInProject(false);
        model.sync();
        final boolean b = model.isRuleSetStoredInProject();
        Assert.assertFalse("the ruleset should'nt be stored in the project", b);
    }

    /**
     * A project may have its ruleset stored in the project own directory. Test set to TRUE.
     */
    @Test
    public void testRuleSetStoredInProjectTRUE() throws PropertiesException, RuleSetNotFoundException {
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        final RuleSetFactory factory = new RuleSetFactory();
        final RuleSet basicRuleSet = factory.createRuleSet("rulesets/java/basic.xml");
        model.setPmdEnabled(true);
        model.setRuleSetStoredInProject(false);
        model.setProjectWorkingSet(null);
        model.setProjectRuleSet(basicRuleSet);
        model.sync();

        model.createDefaultRuleSetFile();
        model.setRuleSetStoredInProject(true);
        model.sync();

        final boolean b = model.isRuleSetStoredInProject();
        final IFile file = this.testProject.getFile(".ruleset");
        final RuleSet projectRuleSet = factory.createRuleSet(file.getLocation().toOSString());
        RuleSet pRuleSet = model.getProjectRuleSet();
        Assert.assertTrue("the ruleset should be stored in the project", b);
        Assert.assertTrue("The project ruleset must be equal to the one found in the project",
                EclipseUtils.assertRuleSetEquals(pRuleSet.getRules(), projectRuleSet.getRules(), System.out));
        // TODO: this assert does not work, as RuleSetReference doesn't implement equals
        // Assert.assertEquals("The project ruleset must be equals to the one found in the project", pRuleSet,
        // projectRuleSet);
    }

    private void dumpRuleSet(final RuleSet ruleSet) {
        System.out.println("Dumping rule set:" + ruleSet.getName());
        for (final Rule rule : ruleSet.getRules()) {
            System.out.println(rule.getName());
        }
        System.out.println();
    }

}
