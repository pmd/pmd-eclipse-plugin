/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.ui.properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.AbstractSWTBotTest;
import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.internal.ResourceUtil;

public class PMDProjectPropertyPageTest extends AbstractSWTBotTest {
    private static final String PROJECT_NAME = PMDProjectPropertyPageTest.class.getSimpleName();

    private IProject testProject;

    @Before
    public void setUp() throws Exception {
        this.testProject = EclipseUtils.createJavaProject(PROJECT_NAME);
        assertTrue("A test project cannot be created; the tests cannot be performed.",
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

    @Test
    public void twoRadioButtonsForRuleSelectionAreShown() {
        SWTBot dialogBot = openProjectProperties();
        SWTBotCheckBox enablePmdCheckbox = dialogBot.checkBox("Enable PMD");
        assertFalse("PMD should not enabled by default", enablePmdCheckbox.isChecked());
        enablePmdCheckbox.click();
        assertNotNull(dialogBot.radio("Use local rules"));
        assertTrue("local rules should be used by default", dialogBot.radio("Use local rules").isSelected());
        assertNotNull(dialogBot.radio("Use the ruleset configured in a project file"));
        dialogBot.button("Cancel").click();
    }

    /**
     * When switching between local rules and ruleset file, only one option of the two
     * should be persisted in the ".pmd" file.
     */
    @Test
    public void verifyProjectPropertiesAfterSwitchingFromRulesetFileToLocal() throws Exception {
        // first enable PMD with local rules (default)
        SWTBot dialogBot = openProjectProperties();
        dialogBot.checkBox("Enable PMD").click();
        dialogBot.button("Apply and Close").click();

        String projectProperties = ResourceUtil.getResourceAsString(testProject, ".pmd");
        assertFalse("No ruleSetFile should be in the properties", projectProperties.contains("<ruleSetFile>"));
        assertTrue("rules should be in the properties", projectProperties.contains("<rules>"));
        assertTrue(projectProperties.contains("<useProjectRuleSet>false</useProjectRuleSet>"));

        // now enable ruleset file
        dialogBot = openProjectProperties();
        dialogBot.radio("Use the ruleset configured in a project file").click();
        dialogBot.button("Apply and Close").click();
        
        bot.shell("PMD Question").bot().button("Yes").click();
        
        projectProperties = ResourceUtil.getResourceAsString(testProject, ".pmd");
        assertTrue("ruleSetFile should be in the properties", projectProperties.contains("<ruleSetFile>"));
        assertFalse("No rules should be in the properties", projectProperties.contains("<rules>"));
        assertTrue(projectProperties.contains("<useProjectRuleSet>true</useProjectRuleSet>"));
    }

    private SWTBot openProjectProperties() {
        SWTBotView projectExplorer = bot.viewByTitle("Project Explorer");
        projectExplorer.show();
        SWTBotTreeItem projectItem = projectExplorer.bot().tree().getTreeItem(PROJECT_NAME);
        projectItem.select();
        projectItem.contextMenu().menu("Properties", false, 0).click();
        SWTBotShell dialog = bot.shell("Properties for " + PROJECT_NAME);
        dialog.bot().tree().getTreeItem("PMD").select();
        return dialog.bot();
    }
}
