/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.actions;

import java.io.InputStream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;

public class PMDCheckActionTest {
    private IProject testProject;

    @Before
    public void setUp() throws Exception {
        // 1. Create a Java project
        this.testProject = EclipseUtils.createJavaProject("PMDCheckActionTest");
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
    public void runCheckAction() throws CoreException, InterruptedException {
        IMarker[] markers = testProject.findMarkers("net.sourceforge.pmd.eclipse.plugin.pmdMarker", true, IResource.DEPTH_INFINITE);
        Assert.assertEquals(0, markers.length);

        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                ISelection selection = new StructuredSelection(testProject);
                PMDCheckAction action = new PMDCheckAction();
                action.selectionChanged(null, selection);
                action.run(null);
            }
        });

        while (isReviewCodeJobStillRunning()) {
            Thread.sleep(500);
        }

        markers = testProject.findMarkers("net.sourceforge.pmd.eclipse.plugin.pmdMarker", true, IResource.DEPTH_INFINITE);
        Assert.assertTrue("at least one marker is expected", markers.length > 0);
    }

    private boolean isReviewCodeJobStillRunning() {
        Job[] jobs = WorkspaceJob.getJobManager().find(null);
        for (Job job : jobs) {
            if ("ReviewCode".equals(job.getName())) {
                return true;
            }
        }
        return false;
    }
}
