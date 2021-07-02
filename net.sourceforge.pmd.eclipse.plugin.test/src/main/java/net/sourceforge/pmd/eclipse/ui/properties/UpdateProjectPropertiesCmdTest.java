/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectPropertiesManager;
import net.sourceforge.pmd.eclipse.runtime.properties.PropertiesException;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;

public class UpdateProjectPropertiesCmdTest {
    private IProject testProject;

    @Before
    public void setUp() throws Exception {
        // 1. Create a Java project
        this.testProject = EclipseUtils.createJavaProject("PMDTestProject");
        Assert.assertTrue("A test project cannot be created; the tests cannot be performed.",
                this.testProject != null && this.testProject.exists() && this.testProject.isAccessible());
    }

    @After
    public void tearDown() throws Exception {
        if (this.testProject != null) {
            if (this.testProject.exists() && this.testProject.isAccessible()) {
                EclipseUtils.removePMDNature(this.testProject);
                this.testProject.refreshLocal(IResource.DEPTH_INFINITE, null);
                this.testProject.delete(true, true, null);
                this.testProject = null;
            } else {
                System.out.println("WARNING: Test Project has not been deleted!");
            }
        }
    }

    /**
     * Bug: when a user deselect a project rule it is not saved
     */
    @Test
    public void testBug() throws PropertiesException {
        // First ensure that the plugin initial ruleset is equal to the project
        // ruleset
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);

        RuleSet projectRuleSet = model.getProjectRuleSet();
        Assert.assertEquals("The project ruleset is not equal to the plugin ruleset",
                PMDPlugin.getDefault().getPreferencesManager().getRuleSet().getRules(), projectRuleSet.getRules());
        int ruleCountBefore = projectRuleSet.getRules().size();

        // 2. remove a rule (keep its name for assertion)
        RuleSet newRuleSet = RuleSetUtil.newEmpty("test-ruleset", "ruleset for unit testing");
        newRuleSet = RuleSetUtil.addRules(newRuleSet, projectRuleSet.getRules());
        final Rule removedRule = newRuleSet.getRuleByName("UnnecessaryParentheses");
        newRuleSet = RuleSetUtil.removeRule(newRuleSet, removedRule);

        final UpdateProjectPropertiesCmd cmd = new UpdateProjectPropertiesCmd();
        cmd.setPmdEnabled(true);
        cmd.setProject(this.testProject);
        cmd.setProjectRuleSet(newRuleSet);
        cmd.setProjectWorkingSet(null);
        cmd.setRuleSetStoredInProject(false);
        cmd.execute();

        // 3. test the rule has correctly been removed
        projectRuleSet = model.getProjectRuleSet();
        Assert.assertEquals("Rule count should be 1 less", ruleCountBefore - 1, projectRuleSet.getRules().size());
        for (Rule r : projectRuleSet.getRules()) {
            if (r.getName().equals(removedRule.getName()) && r.getLanguage() == removedRule.getLanguage()) {
                Assert.fail("The rule has not been removed!");
            }
        }
    }

}
