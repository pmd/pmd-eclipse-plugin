/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.util.internal.IOUtil;
import net.sourceforge.pmd.renderers.HTMLRenderer;
import net.sourceforge.pmd.renderers.TextRenderer;

/**
 * Test the report rendering
 * 
 * @author Philippe Herlin
 * 
 */
public class RenderReportCmdTest {
    private IProject testProject;

    @Before
    public void setUp() throws Exception {

        // 1. Create a Java project
        this.testProject = EclipseUtils.createJavaProject("PMDTestProject");
        Assert.assertTrue("A test project cannot be created; the tests cannot be performed.",
                this.testProject != null && this.testProject.exists() && this.testProject.isAccessible());

        // 2. Create a test source file inside that project
        EclipseUtils.createTestSourceFile(this.testProject);
        try (InputStream is = EclipseUtils.getResourceStream(this.testProject, "/src/Test.java")) {
            Assert.assertNotNull("Cannot find the test source file", is);
        }

        // 3. Enable PMD for the test project
        IProjectProperties properties = PMDPlugin.getDefault().getPropertiesManager()
                .loadProjectProperties(testProject);
        properties.setPmdEnabled(true);
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
     * Test the basic usage of the report rendering command.
     */
    @Test
    public void testRenderReportCmdBasic() throws CoreException {
        final ReviewCodeCmd reviewCmd = new ReviewCodeCmd();
        reviewCmd.addResource(this.testProject);
        reviewCmd.performExecute();
        reviewCmd.join();

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
     * Test text format renderer.
     */
    @Test
    public void testRenderReportCmdText() throws Exception {
        final ReviewCodeCmd reviewCmd = new ReviewCodeCmd();
        reviewCmd.addResource(this.testProject);
        reviewCmd.performExecute();
        reviewCmd.join();

        IMarker[] markers = this.testProject.findMarkers(PMDRuntimeConstants.PMD_MARKER, true, IResource.DEPTH_INFINITE);
        Assert.assertEquals(11, markers.length);

        final RenderReportsCmd cmd = new RenderReportsCmd();
        cmd.setProject(this.testProject);
        cmd.registerRenderer(new TextRenderer(), PMDRuntimeConstants.TXT_REPORT_NAME);
        cmd.performExecute();
        cmd.join();

        final IFolder reportFolder = this.testProject.getFolder(PMDRuntimeConstants.REPORT_FOLDER);
        Assert.assertTrue(reportFolder.exists());

        final IFile reportFile = reportFolder.getFile(PMDRuntimeConstants.TXT_REPORT_NAME);
        Assert.assertTrue(reportFile.exists());
        try (Reader reader = new InputStreamReader(reportFile.getContents(), reportFile.getCharset())) {
            String report = IOUtil.toString(reader);
            Assert.assertEquals(1, countMatches(report, "src/Test.java:5:\tNoPackage:\tNoPackage: All classes, interfaces, enums and annotations must belong to a named package"));
            String[] lines = report.split("\r\n|\n");
            Assert.assertEquals(markers.length, lines.length);
        }

        this.testProject.deleteMarkers(PMDRuntimeConstants.PMD_MARKER, true, IResource.DEPTH_INFINITE);

        if (reportFile.exists()) {
            reportFile.delete(true, false, null);
        }

        if (reportFolder.exists()) {
            reportFolder.delete(true, false, null);
        }
    }

    private static int countMatches(String s, String pattern) {
        int count = 0;
        int index = s.indexOf(pattern);
        while (index != -1) {
            count++;
            index = s.indexOf(pattern, index + pattern.length());
        }
        return count;
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
