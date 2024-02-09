/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.properties;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.LoggingRule;
import net.sourceforge.pmd.eclipse.internal.ResourceUtil;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.cmd.BuildProjectCommand;
import net.sourceforge.pmd.eclipse.runtime.cmd.JobCommandProcessor;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectPropertiesManager;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;
import net.sourceforge.pmd.lang.rule.RuleSet;

public class ExternalRuleSetFileTest {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalRuleSetFileTest.class);

    @org.junit.Rule
    public LoggingRule loggingRule = new LoggingRule();

    private static final String PMD_PROPERTIES_FILENAME = ".pmd";

    private static final String PROJECT_RULESET_FILENAME = ".pmd-ruleset.xml";

    private IProject testProject;


    private void setUpProject(String projectName) {
        try {
            this.testProject = EclipseUtils.createJavaProject(projectName);
            Assert.assertTrue("A test project cannot be created; the tests cannot be performed.",
                    this.testProject != null && this.testProject.exists() && this.testProject.isAccessible());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            if (this.testProject != null) {
                if (this.testProject.exists() && this.testProject.isAccessible()) {
                    this.testProject.delete(true, true, null);
                    this.testProject = null;
                }
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void changedExternalRulesetShouldBeReloaded() throws Exception {
        setUpProject("ExternalRulesetTest");
        // create the ruleset file in the project
        IFile ruleSetFile = this.testProject.getFile(PROJECT_RULESET_FILENAME);
        if (ruleSetFile.exists()) {
            Assert.fail("File " + PROJECT_RULESET_FILENAME + " already exists!");
        }

        try (InputStream ruleset1 = ExternalRuleSetFileTest.class.getResourceAsStream("ruleset1.xml")) {
            ruleSetFile.create(ruleset1, true, null);
        }

        // configure the project to use this
        final UpdateProjectPropertiesCmd cmd = new UpdateProjectPropertiesCmd();
        cmd.setPmdEnabled(true);
        cmd.setProject(this.testProject);
        cmd.setProjectWorkingSet(null);
        cmd.setProjectRuleSetList(Collections.singletonList(RuleSetUtil.newEmpty("empty", "empty")));
        cmd.setRuleSetStoredInProject(true);
        cmd.setRuleSetFile(PROJECT_RULESET_FILENAME);
        cmd.execute();

        // load the project settings - it should have this ruleset now active (1 rule)
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);
        RuleSet projectRuleSet = model.getProjectRuleSet();
        Assert.assertEquals(1, projectRuleSet.getRules().size());

        // now let's change the ruleSetFile without eclipse knowing about it ("externally")
        waitASecond();
        File ruleSetFileReal = ruleSetFile.getLocation().toFile();
        ResourceUtil.copyResource(this, "ruleset2.xml", ruleSetFileReal);

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
    public void changedExternalRulesetShouldBeReloadedForPmdDisabledProjects() throws Exception {
        setUpProject("ExternalRulesetTestPmdDisabled");

        // load the project properties - pmd is not enabled, it uses the default ruleset (more than 1 active rule)
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);
        RuleSet projectRuleSet = model.getProjectRuleSet();
        Assert.assertNotEquals(1, projectRuleSet.getRules().size());

        // now let's change the ruleSetFile without eclipse knowing about it ("externally")
        IFile ruleSetFile = this.testProject.getFile(PROJECT_RULESET_FILENAME);
        if (ruleSetFile.exists()) {
            Assert.fail("File " + PROJECT_RULESET_FILENAME + " already exists!");
        }
        File ruleSetFileReal = ruleSetFile.getLocation().toFile();
        waitASecond();
        ResourceUtil.copyResource(this, "ruleset1.xml", ruleSetFileReal);

        // now create the .pmd project properties without eclipse knowing about it ("externally")
        IFile projectPropertiesFile = this.testProject.getFile(PMD_PROPERTIES_FILENAME);
        if (projectPropertiesFile.exists()) {
            Assert.fail("File .pmd does already exist!");
        }
        File projectPropertiesFileReal = projectPropertiesFile.getLocation().toFile();
        ResourceUtil.copyResource(this, "pmd-properties", projectPropertiesFileReal);

        // the files have been created, but the .pmd file is not reloaded yet, so:
        Assert.assertFalse(model.isNeedRebuild());
        // the model is not updated yet...
        Assert.assertNotEquals(1, model.getProjectRuleSet().getRules().size());
        // but it will be when requesting the project properties again
        final IProjectProperties model2 = mgr.loadProjectProperties(testProject);
        // PMD is still not enabled
        Assert.assertFalse(model.isPmdEnabled());
        // but rebuild is needed (because ruleset changed)
        Assert.assertTrue(model.isNeedRebuild());
        // and the ruleset is loaded
        Assert.assertEquals(1, model2.getProjectRuleSet().getRules().size());
        // the model is still cached, but the rules are updated
        Assert.assertSame(model, model2);
    }

    @Test
    public void externallyChangedProjectPropertiesShouldBeReloaded() throws Exception {
        setUpProject("ProjectPropertiesChanged");
        // configure the project to use PMD
        final UpdateProjectPropertiesCmd cmd = new UpdateProjectPropertiesCmd();
        cmd.setPmdEnabled(true);
        cmd.setProject(this.testProject);
        cmd.setProjectWorkingSet(null);
        RuleSet ruleSet = PMDPlugin.getDefault().getPreferencesManager().getRuleSet();
        cmd.setProjectRuleSetList(Collections.singletonList(ruleSet));
        cmd.setRuleSetStoredInProject(false);
        cmd.execute();

        if (cmd.isNeedRebuild()) {
            final BuildProjectCommand rebuildCmd = new BuildProjectCommand();
            rebuildCmd.setProject(this.testProject);
            rebuildCmd.setUserInitiated(true);
            rebuildCmd.execute();
            JobCommandProcessor.getInstance().waitCommandToFinish(null);
        }


        // load the project settings - it should have this ruleset now active (many rules)
        final IProjectPropertiesManager mgr = PMDPlugin.getDefault().getPropertiesManager();
        final IProjectProperties model = mgr.loadProjectProperties(this.testProject);
        RuleSet projectRuleSet = model.getProjectRuleSet();
        int numberOfRules = ruleSet.size();
        Assert.assertEquals(numberOfRules, projectRuleSet.getRules().size());
        // after the rebuild above, the project should be in a consistent state
        Assert.assertFalse(model.isNeedRebuild());

        // now create a .pmd-ruleset.xml file without eclipse knowing about it ("externally")
        IFile ruleSetFile = this.testProject.getFile(PROJECT_RULESET_FILENAME);
        if (ruleSetFile.exists()) {
            Assert.fail("File " + PROJECT_RULESET_FILENAME + " already exists!");
        }
        File ruleSetFileReal = ruleSetFile.getLocation().toFile();
        ResourceUtil.copyResource(this, "ruleset1.xml", ruleSetFileReal);

        // now overwrite and change the .pmd project properties without eclipse knowing about it ("externally")
        IFile projectPropertiesFile = this.testProject.getFile(PMD_PROPERTIES_FILENAME);
        if (!projectPropertiesFile.exists()) {
            Assert.fail("File .pmd does not exist!");
        }
        File projectPropertiesFileReal = projectPropertiesFile.getLocation().toFile();
        waitASecond();
        ResourceUtil.copyResource(this, "pmd-properties", projectPropertiesFileReal);
        LOG.debug("Overwritten {}", projectPropertiesFile);

        // the model is not updated yet...
        Assert.assertEquals(numberOfRules, model.getProjectRuleSet().getRules().size());
        // but it will be when requesting the project properties again
        final IProjectProperties model2 = mgr.loadProjectProperties(testProject);
        // the file and the ruleset have changed, this should be detected
        Assert.assertTrue(model2.isNeedRebuild());
        // the new rule set should be active now
        Assert.assertEquals(1, model2.getProjectRuleSet().getRules().size());
        // the model is still cached, but the rules are updated
        Assert.assertSame(model, model2);

        // rebuild again
        final BuildProjectCommand rebuildCmd = new BuildProjectCommand();
        rebuildCmd.setProject(this.testProject);
        rebuildCmd.setUserInitiated(true);
        rebuildCmd.execute();
        JobCommandProcessor.getInstance().waitCommandToFinish(null);
        // need rebuild flag should be reset now
        Assert.assertFalse(model.isNeedRebuild());
    }

    // HFS+ under MacOS has a date resolution of 1 second only
    private void waitASecond() throws InterruptedException {
        Thread.sleep(1000);
    }
}
