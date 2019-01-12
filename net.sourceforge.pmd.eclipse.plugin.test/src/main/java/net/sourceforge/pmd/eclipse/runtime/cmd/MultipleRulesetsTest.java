/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectPropertiesManager;

/**
 * This tests execution of PMD with multiple rulesets
 */
public class MultipleRulesetsTest {
    private IProject testProject;

    @Before
    public void setUp() throws Exception {

        // 1. Create a Java project
        this.testProject = EclipseUtils.createJavaProject("PMDTestProject");
        Assert.assertTrue("A test project cannot be created; the tests cannot be performed.",
                this.testProject != null && this.testProject.exists() && this.testProject.isAccessible());

        // 2. Setup test folder
        this.testProject.getFolder("src/main/java").getFullPath().toFile().mkdirs();
        IFolder folder = this.testProject.getFolder("/src/main");
        folder.create(true, true, null);
        folder = folder.getFolder("java");
        folder.create(true, true, null);
        IJavaProject javaProject = JavaCore.create(this.testProject);
        javaProject.setRawClasspath(
                new IClasspathEntry[] { JavaCore.newSourceEntry(testProject.getFolder("src/main/java").getFullPath()),
                    JavaCore.newContainerEntry(new Path("org.eclipse.jdt.launching.JRE_CONTAINER")) },
                null);

        // 3. Create test sources
        folder.getFolder("first").create(true, true, null);
        EclipseUtils.createTestSourceFile(testProject, "/src/main/java/first/First.java",
                IOUtils.toString(MultipleRulesetsTest.class.getResourceAsStream("multiplerulesets/First.java"),
                        StandardCharsets.UTF_8.name()));
        folder.getFolder("second").create(true, true, null);
        EclipseUtils.createTestSourceFile(testProject, "/src/main/java/second/Second.java",
                IOUtils.toString(MultipleRulesetsTest.class.getResourceAsStream("multiplerulesets/Second.java"),
                        StandardCharsets.UTF_8.name()));
        folder.getFolder("third").create(true, true, null);
        EclipseUtils.createTestSourceFile(testProject, "/src/main/java/third/Third.java",
                IOUtils.toString(MultipleRulesetsTest.class.getResourceAsStream("multiplerulesets/Third.java"),
                        StandardCharsets.UTF_8.name()));

        // 4. Copy rulesets
        EclipseUtils.createTestSourceFile(testProject, "/ruleset1.xml",
                IOUtils.toString(MultipleRulesetsTest.class.getResourceAsStream("multiplerulesets/ruleset1.xml"),
                        StandardCharsets.UTF_8.name()));
        EclipseUtils.createTestSourceFile(testProject, "/ruleset2.xml",
                IOUtils.toString(MultipleRulesetsTest.class.getResourceAsStream("multiplerulesets/ruleset2.xml"),
                        StandardCharsets.UTF_8.name()));

        // 5. Enable and configure PMD for the test project
        IProjectPropertiesManager propertiesManager = PMDPlugin.getDefault().getPropertiesManager();
        IProjectProperties properties = propertiesManager.loadProjectProperties(testProject);
        properties.setPmdEnabled(true);
        properties.setRuleSetFile("ruleset1.xml,ruleset2.xml");
        properties.setRuleSetStoredInProject(true);
        propertiesManager.storeProjectProperties(properties);
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

    /**
     * Test the basic usage of the processor command
     */
    @Test
    public void testReviewCmdBasic() throws CoreException {
        final ReviewCodeCmd cmd = new ReviewCodeCmd();
        cmd.addResource(this.testProject);
        cmd.performExecute();
        cmd.join();

        IMarker[] markersFirst = this.testProject.getFile("src/main/java/first/First.java")
                .findMarkers(PMDRuntimeConstants.PMD_MARKER_1, false, 1);
        Assert.assertEquals(2, markersFirst.length);
        assertHasRuleViolation(markersFirst, "ClassNamingConventions");
        assertHasRuleViolation(markersFirst, "UseUtilityClass");

        IMarker[] markersSecond = this.testProject.getFile("src/main/java/second/Second.java")
                .findMarkers(PMDRuntimeConstants.PMD_MARKER_1, false, 1);
        Assert.assertEquals(1, markersSecond.length);
        assertHasRuleViolation(markersSecond, "ClassNamingConventions");

        IMarker[] markersThird = this.testProject.getFile("src/main/java/third/Third.java")
                .findMarkers(PMDRuntimeConstants.PMD_MARKER_1, false, 1);
        Assert.assertEquals(1, markersThird.length);
        assertHasRuleViolation(markersThird, "UseUtilityClass");
    }

    private void assertHasRuleViolation(IMarker[] markers, String rulename) throws CoreException {
        boolean found = false;
        for (IMarker marker : markers) {
            if (marker.getAttribute(PMDRuntimeConstants.KEY_MARKERATT_RULENAME).equals(rulename)) {
                found = true;
                break;
            }
        }
        if (!found) {
            Assert.fail("Expected violation for rule " + rulename + ", but no violation found!");
        }
    }
}
