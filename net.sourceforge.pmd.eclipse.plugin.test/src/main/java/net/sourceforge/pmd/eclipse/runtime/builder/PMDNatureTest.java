/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.EclipseUtils;

public class PMDNatureTest {

    @Test
    public void addPMDNatureForNonJavaProject() throws Exception {
        IProject testProject = EclipseUtils.createProject("TestNonJavaProject");
        Assert.assertTrue("A test project cannot be created; the tests cannot be performed.",
                testProject != null && testProject.exists() && testProject.isAccessible());
        Assert.assertFalse(testProject.hasNature(JavaCore.NATURE_ID));
        Assert.assertFalse(testProject.hasNature(PMDNature.PMD_NATURE));

        PMDNature.addPMDNature(testProject, null);

        Assert.assertTrue(testProject.hasNature(PMDNature.PMD_NATURE));

        if (testProject.exists() && testProject.isAccessible()) {
            EclipseUtils.removePMDNature(testProject);
            testProject.refreshLocal(IResource.DEPTH_INFINITE, null);
            testProject.delete(true, true, null);
            testProject = null;
        }
    }
}
