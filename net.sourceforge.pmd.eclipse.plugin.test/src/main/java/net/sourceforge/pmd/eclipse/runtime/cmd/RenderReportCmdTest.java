/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.renderers.HTMLRenderer;

/**
 * Test the report rendering
 * 
 * @author Philippe Herlin
 * 
 */
public class RenderReportCmdTest {
    private IProject testProject;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {

        // 1. Create a Java project
        this.testProject = EclipseUtils.createJavaProject("PMDTestProject");
        Assert.assertTrue("A test project cannot be created; the tests cannot be performed.",
                this.testProject != null && this.testProject.exists() && this.testProject.isAccessible());

        // 2. Create a test source file inside that project
        EclipseUtils.createTestSourceFile(this.testProject);
        final InputStream is = EclipseUtils.getResourceStream(this.testProject, "/src/Test.java");
        Assert.assertNotNull("Cannot find the test source file", is);
        is.close();

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
     * Test the basic usage of the report rendering command
     * 
     */
    @Test
    public void testRenderReportCmdBasic() throws CoreException {
        final ReviewCodeCmd reviewCmd = new ReviewCodeCmd();
        reviewCmd.addResource(this.testProject);
        reviewCmd.performExecute();

        final RenderReportsCmd cmd = new RenderReportsCmd();
        cmd.setProject(this.testProject);
        cmd.registerRenderer(new HTMLRenderer(), PMDRuntimeConstants.HTML_REPORT_NAME);
        cmd.performExecute();
        cmd.join();

        final IFolder reportFolder = this.testProject.getFolder(PMDRuntimeConstants.REPORT_FOLDER);
        Assert.assertTrue(reportFolder.exists());

        final IFile reportFile = reportFolder.getFile(PMDRuntimeConstants.HTML_REPORT_NAME);
        Assert.assertTrue(reportFile.exists());

        this.testProject.deleteMarkers(PMDRuntimeConstants.PMD_MARKER, true, IResource.DEPTH_INFINITE);

        if (reportFile.exists()) {
            reportFile.delete(true, false, null);
        }

        if (reportFolder.exists()) {
            reportFolder.delete(true, false, null);
        }
    }

    /**
     * Test robustness #1
     */
    @Test(expected = IllegalStateException.class)
    public void testRenderReportCmdNullArg1() {
        final RenderReportsCmd cmd = new RenderReportsCmd();
        cmd.setProject(null);
        cmd.registerRenderer(new HTMLRenderer(), PMDRuntimeConstants.HTML_REPORT_NAME);
        cmd.performExecute();
    }

    /**
     * Test robustness #2
     */
    @Test(expected = IllegalStateException.class)
    public void testRenderReportCmdNullArg2() {
        final RenderReportsCmd cmd = new RenderReportsCmd();
        cmd.setProject(this.testProject);
        cmd.registerRenderer(null, PMDRuntimeConstants.HTML_REPORT_NAME);
        cmd.performExecute();
    }

    /**
     * Test robustness #3
     */
    @Test(expected = IllegalStateException.class)
    public void testRenderReportCmdNullArg3() {
        final RenderReportsCmd cmd = new RenderReportsCmd();
        cmd.setProject(this.testProject);
        cmd.registerRenderer(new HTMLRenderer(), null);
        cmd.performExecute();
    }

    /**
     * Test robustness #4
     */
    @Test(expected = IllegalStateException.class)
    public void testRenderReportCmdNullArg4() {
        final RenderReportsCmd cmd = new RenderReportsCmd();
        cmd.setProject(null);
        cmd.registerRenderer(null, PMDRuntimeConstants.HTML_REPORT_NAME);
        cmd.performExecute();
    }

    /**
     * Test robustness #5
     */
    @Test(expected = IllegalStateException.class)
    public void testRenderReportCmdNullArg5() {
        final RenderReportsCmd cmd = new RenderReportsCmd();
        cmd.setProject(null);
        cmd.registerRenderer(new HTMLRenderer(), null);
        cmd.performExecute();
    }

    /**
     * Test robustness #6
     */
    @Test(expected = IllegalStateException.class)
    public void testRenderReportCmdNullArg6() {
        final RenderReportsCmd cmd = new RenderReportsCmd();
        cmd.setProject(this.testProject);
        cmd.registerRenderer(null, null);
        cmd.performExecute();
    }

    /**
     * Test robustness #7
     */
    @Test(expected = IllegalStateException.class)
    public void testRenderReportCmdNullArg7() {
        final RenderReportsCmd cmd = new RenderReportsCmd();
        cmd.setProject(null);
        cmd.registerRenderer(null, null);
        cmd.performExecute();
    }
}
