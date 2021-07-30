/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.runtime.properties.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.cmd.ReviewCodeCmd;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.ui.actions.internal.InternalRuleSetUtil;

public class ProjectPropertiesImplTest {
    private IProject testProject;

    @Before
    public void setUp() throws Exception {
        // 1. Create a Java project
        this.testProject = EclipseUtils.createJavaProject("ProjectPropertiesImplTest");
        Assert.assertTrue("A test project cannot be created; the tests cannot be performed.",
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
    public void projectWithIncludesExcludes() throws Exception {
        testProject.getFolder("/src/main").create(true, true, new NullProgressMonitor());
        testProject.getFolder("/src/main/java").create(true, true, new NullProgressMonitor());
        testProject.getFolder("/src/main/resources").create(true, true, new NullProgressMonitor());
        IFile testFile = EclipseUtils.createTestSourceFile(testProject);
        testFile.move(testProject.getFile("/src/main/java/Test.java").getFullPath(),
                true, new NullProgressMonitor());
        testFile = testProject.getFile("/src/main/java/Test.java");

        IJavaProject javaProject = JavaCore.create(testProject);
        IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
        List<IClasspathEntry> newClasspath = new ArrayList<>();
        for (IClasspathEntry entry : rawClasspath) {
            if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE) {
                newClasspath.add(entry);
            }
        }
        newClasspath.add(JavaCore.newSourceEntry(new Path("/ProjectPropertiesImplTest/src/main/java"),
                new Path[] {new Path("**/*.java")},
                new Path[0], null));
        newClasspath.add(JavaCore.newSourceEntry(new Path("/ProjectPropertiesImplTest/src/main/resources"),
                new Path[0],
                new Path[] {new Path("**")}, null));
        javaProject.setRawClasspath(newClasspath.toArray(new IClasspathEntry[0]), new NullProgressMonitor());

        String expectedPattern = testProject.getFolder("/src/main/java").getLocation().toPortableString();
        expectedPattern += "/.*/[^/]*\\.java";

        IProjectProperties projectProperties = PMDPlugin.getDefault().getPropertiesManager().loadProjectProperties(testProject);
        Set<String> includePatterns = projectProperties.getBuildPathIncludePatterns();
        Collection<Pattern> patterns = InternalRuleSetUtil.convertStringPatterns(includePatterns);

        Assert.assertEquals(1, includePatterns.size());
        Assert.assertEquals(1, patterns.size());
        Assert.assertEquals(includePatterns.toString(), patterns.toString());
        Assert.assertEquals("[" + expectedPattern + "]", patterns.toString());

        // now run a review
        projectProperties.setPmdEnabled(true);
        ReviewCodeCmd cmd = new ReviewCodeCmd();
        cmd.addResource(testProject);
        cmd.performExecute();
        cmd.join();

        IMarker[] markers = testFile.findMarkers(PMDRuntimeConstants.PMD_MARKER, true, 1);
        Assert.assertTrue(markers.length > 0);
        boolean found = false;
        for (IMarker marker : markers) {
            if ("EmptyCatchBlock".equals(marker.getAttribute(PMDRuntimeConstants.KEY_MARKERATT_RULENAME))) {
                found = true;
            }
        }
        Assert.assertTrue("EmptyCatchBlock marker is missing", found);
    }
}
