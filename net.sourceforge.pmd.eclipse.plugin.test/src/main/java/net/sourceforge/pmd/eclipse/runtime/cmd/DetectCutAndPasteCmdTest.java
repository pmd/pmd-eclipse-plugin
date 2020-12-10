/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.cpd.SimpleRenderer;
import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;

/**
 * Test the CPD command
 * 
 * @author Philippe Herlin
 * 
 */
public class DetectCutAndPasteCmdTest {
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

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @After
    public void tearDown() throws Exception {
        if (this.testProject != null) {
            if (this.testProject.exists() && this.testProject.isAccessible()) {
                EclipseUtils.removePMDNature(this.testProject);
                // this.testProject.refreshLocal(IResource.DEPTH_INFINITE,
                // null);
                // Thread.sleep(500);
                // this.testProject.delete(true, true, null);
                // this.testProject = null;
            }
        }
    }

    /**
     * Test the basic usage of the cpd command
     * 
     */
    @Test
    public void testDetectCutAndPasteCmdBasic1() throws CoreException {
        final DetectCutAndPasteCmd cmd = new DetectCutAndPasteCmd();
        cmd.setProject(this.testProject);
        cmd.setCPDRenderer(new SimpleRenderer());
        cmd.setReportName(PMDRuntimeConstants.SIMPLE_CPDREPORT_NAME);
        cmd.setCreateReport(true);
        cmd.setLanguage("java");
        cmd.setMinTileSize(10);
        cmd.performExecute();
        cmd.join();

        final IFolder reportFolder = this.testProject.getFolder(PMDRuntimeConstants.REPORT_FOLDER);
        Assert.assertTrue("The report folder doesn't exist: " + reportFolder, reportFolder.exists());

        final IFile reportFile = reportFolder.getFile(PMDRuntimeConstants.SIMPLE_CPDREPORT_NAME);
        Assert.assertTrue("The report file doesn't exist: " + reportFile, reportFile.exists());

        if (reportFile.exists()) {
            reportFile.delete(true, false, null);
        }

        if (reportFolder.exists()) {
            reportFolder.delete(true, false, null);
        }
    }

    /**
     * Test the basic usage of the cpd command
     * 
     */
    @Test
    public void testDetectCutAndPasteCmdBasic2() throws CoreException {
        final DetectCutAndPasteCmd cmd = new DetectCutAndPasteCmd();
        cmd.setProject(this.testProject);
        cmd.setCreateReport(false);
        cmd.setLanguage("java");
        cmd.setMinTileSize(10);
        cmd.performExecute();
        cmd.join();

        final IFolder reportFolder = this.testProject.getFolder(PMDRuntimeConstants.REPORT_FOLDER);
        Assert.assertFalse(reportFolder.exists());

        final IFile reportFile = reportFolder.getFile(PMDRuntimeConstants.SIMPLE_CPDREPORT_NAME);
        Assert.assertFalse(reportFile.exists());
    }

    /**
     * Test robustness #1
     */
    @Test(expected = IllegalStateException.class)
    public void testDetectCutAndPasteCmdNullArg1() {
        final DetectCutAndPasteCmd cmd = new DetectCutAndPasteCmd();
        cmd.setProject(null);
        cmd.setCPDRenderer(new SimpleRenderer());
        cmd.setReportName(PMDRuntimeConstants.SIMPLE_CPDREPORT_NAME);
        cmd.performExecute();
    }

    /**
     * Test robustness #2
     */
    @Test(expected = IllegalStateException.class)
    public void testDetectCutAndPasteCmdNullArg2() {
        final DetectCutAndPasteCmd cmd = new DetectCutAndPasteCmd();
        cmd.setProject(this.testProject);
        cmd.setCPDRenderer(null);
        cmd.setReportName(PMDRuntimeConstants.SIMPLE_CPDREPORT_NAME);
        cmd.performExecute();
    }

    /**
     * Test robustness #3
     */
    @Test(expected = IllegalStateException.class)
    public void testDetectCutAndPasteCmdNullArg3() {
        final DetectCutAndPasteCmd cmd = new DetectCutAndPasteCmd();
        cmd.setProject(this.testProject);
        cmd.setCPDRenderer(new SimpleRenderer());
        cmd.setReportName(null);
        cmd.performExecute();
    }

    /**
     * Test robustness #4
     */
    @Test(expected = IllegalStateException.class)
    public void testDetectCutAndPasteCmdNullArg4() {
        final DetectCutAndPasteCmd cmd = new DetectCutAndPasteCmd();
        cmd.setProject(null);
        cmd.setCPDRenderer(null);
        cmd.setReportName(PMDRuntimeConstants.SIMPLE_CPDREPORT_NAME);
        cmd.performExecute();
    }

    /**
     * Test robustness #5
     */
    @Test(expected = IllegalStateException.class)
    public void testDetectCutAndPasteCmdNullArg5() {
        final DetectCutAndPasteCmd cmd = new DetectCutAndPasteCmd();
        cmd.setProject(null);
        cmd.setCPDRenderer(new SimpleRenderer());
        cmd.setReportName(null);
        cmd.performExecute();
    }

    /**
     * Test robustness #6
     */
    @Test(expected = IllegalStateException.class)
    public void testDetectCutAndPasteCmdNullArg6() {
        final DetectCutAndPasteCmd cmd = new DetectCutAndPasteCmd();
        cmd.setProject(this.testProject);
        cmd.setCPDRenderer(null);
        cmd.setReportName(null);
        cmd.performExecute();
    }

    /**
     * Test robustness #7
     */
    @Test(expected = IllegalStateException.class)
    public void testDetectCutAndPasteCmdNullArg7() {
        final DetectCutAndPasteCmd cmd = new DetectCutAndPasteCmd();
        cmd.setProject(null);
        cmd.setCPDRenderer(null);
        cmd.setReportName(null);
        cmd.performExecute();
    }
}
