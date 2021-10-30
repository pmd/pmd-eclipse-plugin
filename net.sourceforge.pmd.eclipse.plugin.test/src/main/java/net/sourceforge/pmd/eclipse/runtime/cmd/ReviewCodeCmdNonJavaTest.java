/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.WaitingMonitor;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.builder.PMDNature;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;

public class ReviewCodeCmdNonJavaTest {

    @Test
    public void checkCodeForNonJavaProject() throws Exception {
        IProject testProject = EclipseUtils.createProject("ReviewCodeCmdNonJavaTest");
        Assert.assertTrue("A test project cannot be created; the tests cannot be performed.",
                testProject != null && testProject.exists() && testProject.isAccessible());
        Assert.assertFalse(testProject.hasNature(JavaCore.NATURE_ID));
        Assert.assertFalse(testProject.hasNature(PMDNature.PMD_NATURE));

        PMDNature.addPMDNature(testProject, null);
        Assert.assertTrue(testProject.hasNature(PMDNature.PMD_NATURE));

        // 2. Create a test source file inside that project
        IFolder testFolder = testProject.getFolder("/src");
        if (testFolder.exists()) {
            testFolder.delete(true, null);
        }
        testFolder.create(true, true, null);
        IFile testFile = testFolder.getFile("somefile.js");
        InputStream is = new ByteArrayInputStream("function() { var s = 'test file content'; }".getBytes("UTF-8"));
        if (testFile.exists() && testFile.isAccessible()) {
            testFile.setContents(is, true, false, null);
        } else {
            testFile.create(is, true, null);
        }
        testProject.refreshLocal(IResource.DEPTH_INFINITE, null);

        is = EclipseUtils.getResourceStream(testProject, "/src/somefile.js");
        Assert.assertNotNull("Cannot find the test source file", is);
        is.close();
        testProject.refreshLocal(IResource.DEPTH_INFINITE, null);

        // 3. Enable PMD for the test project
        IProjectProperties properties = PMDPlugin.getDefault().getPropertiesManager().loadProjectProperties(testProject);
        properties.setPmdEnabled(true);

        // explicitly request full build to prevent automatic (parallel) building later on
        WaitingMonitor waitingMonitor = new WaitingMonitor();
        testProject.build(IncrementalProjectBuilder.FULL_BUILD, waitingMonitor);
        waitingMonitor.await();
        EclipseUtils.waitForJobs();

        ReviewCodeCmd cmd = new ReviewCodeCmd();
        cmd.addResource(testProject);
        cmd.performExecute();
        cmd.join();

        // 2 files are there: .project and src/somefile.js, but only the javascript file is considered
        // due to file extensions -> step count = 1
        Assert.assertEquals(1, cmd.getStepCount());
        // only one file has an extension, that could be mapped to a language, and therefore pmd was executed
        Assert.assertEquals(1, cmd.getFileCount());

        if (testProject.exists() && testProject.isAccessible()) {
            EclipseUtils.removePMDNature(testProject);
            testProject.refreshLocal(IResource.DEPTH_INFINITE, null);
            testProject.delete(true, true, null);
            testProject = null;
        }
    }
}
