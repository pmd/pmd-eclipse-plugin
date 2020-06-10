/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.properties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.cmd.BuildProjectCommand;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectPropertiesManager;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;

public class ExternalRuleSetFileTest {
    private static final String PROJECT_RULESET_FILENAME = ".pmd-ruleset.xml";

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
        try {
            // 1. Delete the test project
            if (this.testProject != null) {
                if (this.testProject.exists() && this.testProject.isAccessible()) {
                    this.testProject.delete(true, true, null);
                    this.testProject = null;
                }
            }
        } catch (final Exception e) {
            System.out.println("Exception " + e.getClass().getName() + " when tearing down. Ignored.");
        }
    }

    @Test
    public void changedExternalRulesetShouldBeReloaded() throws Exception {
        InputStream ruleset1 = ExternalRuleSetFileTest.class.getResourceAsStream("ruleset1.xml");

        // create the ruleset file in the project
        IFile ruleSetFile = this.testProject.getFile(PROJECT_RULESET_FILENAME);
        if (ruleSetFile.exists()) {
            Assert.fail("File " + PROJECT_RULESET_FILENAME + " already exists!");
        }
        try {
            ruleSetFile.create(ruleset1, true, null);
        } finally {
            ruleset1.close();
        }

        // configure the project to use this
        final UpdateProjectPropertiesCmd cmd = new UpdateProjectPropertiesCmd();
        cmd.setPmdEnabled(true);
        cmd.setProject(this.testProject);
        cmd.setProjectWorkingSet(null);
        cmd.setProjectRuleSets(new RuleSets(RuleSetUtil.newEmpty("empty", "empty")));
        cmd.setRuleSetStoredInProject(true);
        cmd.setRuleSetFile(PROJECT_RULESET_FILENAME);
        cmd.execute();

        // load the project settings - it should have this ruleset now active (1 rule)
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);
        RuleSet projectRuleSet = model.getProjectRuleSet();
        Assert.assertEquals(1, projectRuleSet.getRules().size());

        // we need to wait a bit, so that the modified timestamp of the file becomes actually different
        Thread.sleep(100);

        // now let's change the ruleSetFile without eclipse knowing about it ("externally")
        File ruleSetFileReal = ruleSetFile.getLocation().toFile();
        copyResource("ruleset2.xml", ruleSetFileReal);

        // the file has changed, this should be detected
        Assert.assertTrue(model.isNeedRebuild());
        // the model is not updated yet...
        Assert.assertEquals(1, model.getProjectRuleSet().getRules().size());
        // but it will be when requesting the project properties again
        final IProjectProperties model2 = mgr.loadProjectProperties(testProject);
        Assert.assertEquals(2, model2.getProjectRuleSet().getRules().size());
        // the model is still cached, but the rules are updated
        Assert.assertSame(model, model2);
    }

    @Test
    public void externallyChangedProjectPropertiesShouldBeReloaded() throws Exception {
        // configure the project to use PMD
        final UpdateProjectPropertiesCmd cmd = new UpdateProjectPropertiesCmd();
        cmd.setPmdEnabled(true);
        cmd.setProject(this.testProject);
        cmd.setProjectWorkingSet(null);
        RuleSets rulesets = new RuleSets(PMDPlugin.getDefault().getPreferencesManager().getRuleSet());
        cmd.setProjectRuleSets(rulesets);
        cmd.setRuleSetStoredInProject(false);
        cmd.execute();

        if (cmd.isNeedRebuild()) {
            final BuildProjectCommand rebuildCmd = new BuildProjectCommand();
            rebuildCmd.setProject(this.testProject);
            rebuildCmd.setUserInitiated(true);
            rebuildCmd.execute();
        }


        // load the project settings - it should have this ruleset now active (many rules)
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);
        RuleSet projectRuleSet = model.getProjectRuleSet();
        int numberOfRules = rulesets.getAllRules().size();
        Assert.assertEquals(numberOfRules, projectRuleSet.getRules().size());
        // after the rebuild above, the project should be in a consistent state
        Assert.assertFalse(model.isNeedRebuild());

        // now create a .pmd-ruleset.xml file without eclipse knowing about it ("externally")
        IFile ruleSetFile = this.testProject.getFile(PROJECT_RULESET_FILENAME);
        if (ruleSetFile.exists()) {
            Assert.fail("File " + PROJECT_RULESET_FILENAME + " already exists!");
        }
        File ruleSetFileReal = ruleSetFile.getLocation().toFile();
        copyResource("ruleset1.xml", ruleSetFileReal);

        // we need to wait a bit, so that the modified timestamp of the file becomes actually different
        Thread.sleep(100);

        // now overwrite and change the .pmd project properties without eclipse knowing about it ("externally")
        IFile projectPropertiesFile = this.testProject.getFile(".pmd");
        if (!projectPropertiesFile.exists()) {
            Assert.fail("File .pmd does not exist!");
        }
        File projectPropertiesFileReal = projectPropertiesFile.getLocation().toFile();
        copyResource("pmd-properties", projectPropertiesFileReal);

        // the model is not updated yet...
        Assert.assertEquals(numberOfRules, model.getProjectRuleSet().getRules().size());
        // but it will be when requesting the project properties again
        final IProjectProperties model2 = mgr.loadProjectProperties(testProject);
        // the file and the ruleset have changed, this should be detected
        Assert.assertTrue(model.isNeedRebuild());
        // the new rule set should be active now
        Assert.assertEquals(1, model2.getProjectRuleSet().getRules().size());
        // the model is still cached, but the rules are updated
        Assert.assertSame(model, model2);

        // rebuild again
        final BuildProjectCommand rebuildCmd = new BuildProjectCommand();
        rebuildCmd.setProject(this.testProject);
        rebuildCmd.setUserInitiated(true);
        rebuildCmd.execute();
        // need rebuild flag should be reset now
        Assert.assertFalse(model.isNeedRebuild());
    }

    private static void copyResource(String resource, File target) throws IOException {
        try (FileOutputStream out = new FileOutputStream(target);
                InputStream ruleset2 = ExternalRuleSetFileTest.class.getResourceAsStream(resource)) {
            int count;
            byte[] buffer = new byte[8192];
            count = ruleset2.read(buffer);
            while (count > -1) {
                out.write(buffer, 0, count);
                count = ruleset2.read(buffer);
            }
        }
    }
}
