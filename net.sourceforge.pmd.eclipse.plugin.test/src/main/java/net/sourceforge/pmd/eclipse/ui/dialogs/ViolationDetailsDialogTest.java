/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.dialogs;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.AbstractSWTBotTest;
import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;

public class ViolationDetailsDialogTest extends AbstractSWTBotTest {
    private static final String PROJECT_NAME = ViolationDetailsDialogTest.class.getSimpleName();

    private IProject testProject;

    @Before
    public void setUp() throws Exception {
        // 1. Create a Java project
        this.testProject = EclipseUtils.createJavaProject(PROJECT_NAME);
        Assert.assertTrue("A test project cannot be created; the tests cannot be performed.",
                this.testProject != null && this.testProject.exists() && this.testProject.isAccessible());

        // 2. Create a test source file inside that project
        EclipseUtils.createTestSourceFile(testProject, "/src/MyInterface.java", "public interface MyInterface {\n    public void run();\n}\n".replaceAll("\\R", System.lineSeparator()));
        try (InputStream is = EclipseUtils.getResourceStream(this.testProject, "/src/MyInterface.java")) {
            Assert.assertNotNull("Cannot find the test source file", is);
        }

        // 3. Enable PMD for the test project
        IProjectProperties properties = PMDPlugin.getDefault().getPropertiesManager()
                .loadProjectProperties(testProject);
        properties.setPmdEnabled(true);
        properties.sync();
    }

    @After
    public void tearDown() throws Exception {
        try {
            if (this.testProject != null) {
                if (this.testProject.exists() && this.testProject.isAccessible()) {
                    EclipseUtils.removePMDNature(this.testProject);
                    this.testProject.refreshLocal(IResource.DEPTH_INFINITE, null);
                    this.testProject.delete(true, true, null);
                    this.testProject = null;
                }
            }
        } catch (final Exception e) {
            System.out.println("Exception " + e.getClass().getName() + " when tearing down. Ignored.");
        }
    }

    @Test
    public void openDialogViaProblemView() throws Exception {
        buildAndWaitForViolations();
        openJavaPerspective();

        SWTBotView problemsView = bot.viewByPartName("Problems");
        String markerText = "UnnecessaryModifier: Unnecessary modifier 'public' on method 'run': the method is declared in an interface type";

        problemsView.bot().waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                try {
                    SWTBotTreeItem item = bot.tree().getTreeItem("Warnings (4 items)").expand();
                    item.getNode(markerText);
                } catch (WidgetNotFoundException e) {
                    return false;
                }
                return true;
            }

            @Override
            public String getFailureMessage() {
                return "Marker not found";
            }
        });
        SWTBotTreeItem item = problemsView.bot().tree().getTreeItem("Warnings (4 items)").expand();
        SWTBotTreeItem markerItem = item.getNode(markerText).select();
        markerItem.contextMenu("Show details...").click();

        assertDialog();
    }

    @Test
    public void openDialogViaViolationOutlineView() throws Exception {
        buildAndWaitForViolations();
        openPMDPerspective();
        
        SWTBotTree projectTree = bot.viewByPartName("Package Explorer").bot().tree();
        SWTBotTreeItem item = projectTree.getTreeItem("ViolationDetailsDialogTest").expand();
        item = item.getNode("src").expand();
        item = item.getNode("(default package)").expand();
        item = item.getNode("MyInterface.java").select();
        item.doubleClick();

        SWTBotView outlineView = bot.viewByPartName("Violations Outline");
        int row = outlineView.bot().table().indexOf("UnnecessaryModifier", 3);
        SWTBotTableItem tableItem = outlineView.bot().table().getTableItem(row);
        tableItem.select();
        tableItem.contextMenu("Show details ...").click();

        assertDialog();
    }

    private void assertDialog() {
        SWTBotShell dialog = bot.shell("PMD Plugin: Violation Details");
        String message = dialog.bot().text(0).getText();
        dialog.bot().button("Close").click();

        assertEquals("Unnecessary modifier 'public' on method 'run': the method is declared in an interface type", message);
    }

    private void buildAndWaitForViolations() throws CoreException {
        testProject.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());

        bot.waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                IMarker[] markers = testProject.findMarkers(PMDRuntimeConstants.PMD_MARKER, true, IResource.DEPTH_INFINITE);
                return markers.length > 0;
            }

            @Override
            public String getFailureMessage() {
                return "At least one marker is expected";
            }
        });
    }

    private static void openPMDPerspective() throws InterruptedException {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    IWorkbench workbench = PlatformUI.getWorkbench();
                    IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
                    workbench.showPerspective(PMDRuntimeConstants.ID_PERSPECTIVE, activeWorkbenchWindow);
                } catch (WorkbenchException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
