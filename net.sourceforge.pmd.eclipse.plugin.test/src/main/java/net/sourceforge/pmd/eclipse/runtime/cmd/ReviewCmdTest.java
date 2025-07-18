/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.LoggingRule;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.lang.rule.RuleSet;

/**
 * This tests the PMD Processor command
 * 
 * @author Philippe Herlin
 * 
 */
public class ReviewCmdTest {
    private IProject testProject;

    @org.junit.Rule
    public LoggingRule loggingRule = new LoggingRule();

    @Before
    public void setUp() throws Exception {

        // 1. Create a Java project
        this.testProject = EclipseUtils.createJavaProject("ReviewCmdTest");
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
     * Test the basic usage of the processor command
     * 
     */
    @Test
    public void testReviewCmdBasic() throws CoreException {
        final ReviewCodeCmd cmd = new ReviewCodeCmd();
        cmd.addResource(this.testProject);
        cmd.performExecute();
        cmd.join();
        final Map<IFile, Set<MarkerInfo2>> markers = cmd.getMarkers();

        // We do not test PMD, only a non-empty report is enough
        Assert.assertNotNull(markers);
        Assert.assertFalse("Report is empty", markers.isEmpty());

        // test the marker types - they should be problem markers...
        final IFile sourceFile = this.testProject.getFile("/src/Test.java");
        List<IMarker> imarkers = new ArrayList<>();
        for (String markerType : PMDRuntimeConstants.RULE_MARKER_TYPES) {
            imarkers.addAll(Arrays.asList(sourceFile.findMarkers(markerType, false, IResource.DEPTH_ONE)));
        }
        Assert.assertEquals(markers.get(sourceFile).size(), imarkers.size());
        for (IMarker marker : imarkers) {
            Assert.assertTrue(marker.isSubtypeOf(IMarker.PROBLEM));
            Assert.assertTrue(((String) marker.getAttribute(IMarker.MESSAGE))
                    .startsWith((String) marker.getAttribute(PMDRuntimeConstants.KEY_MARKERATT_RULENAME)));
        }
    }

    private IFile createMissingOverrideTestCase(IProject project) throws Exception {
        EclipseUtils.createTestSourceFile(project, "/src/MyInterface.java", "public interface MyInterface { void run(); }");
        IFile sourceFile = EclipseUtils.createTestSourceFile(project, "/src/Foo.java",
                "class Foo implements MyInterface {\n"
              + "  void run() {\n" // line 2
              + "  }\n"
              + "}");

        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());

        return sourceFile;
    }

    /**
     * https://sourceforge.net/p/pmd/bugs/1145/
     */
    @Test
    public void testProjectBuildPath() throws Exception {
        IProjectProperties properties = PMDPlugin.getDefault().getPropertiesManager()
                .loadProjectProperties(testProject);
        Rule missingOverrideRule = properties.getProjectRuleSet().getRuleByName("MissingOverride");
        RuleSet projectRuleSet = RuleSetUtil.newSingle(missingOverrideRule);
        properties.setProjectRuleSet(projectRuleSet);
        boolean oldSetting = PMDPlugin.getDefault().getPreferencesManager().loadPreferences()
                .isProjectBuildPathEnabled();

        try {
            PMDPlugin.getDefault().getPreferencesManager().loadPreferences().setProjectBuildPathEnabled(true);
            IFile sourceFile = createMissingOverrideTestCase(testProject);

            ReviewCodeCmd cmd = new ReviewCodeCmd();
            cmd.addResource(testProject);
            cmd.performExecute();
            cmd.join();
            Map<IFile, Set<MarkerInfo2>> markers = cmd.getMarkers();
            // with type resolution, we detect missing override annotation
            Assert.assertFalse("Type Resolution didn't work", markers.get(sourceFile).isEmpty());

            // without type resolution, there is no violation
            PMDPlugin.getDefault().getPreferencesManager().loadPreferences().setProjectBuildPathEnabled(false);
            cmd = new ReviewCodeCmd();
            cmd.addResource(testProject);
            cmd.performExecute();
            cmd.join();
            markers = cmd.getMarkers();
            // there is no violation expected without type resolution
            Assert.assertTrue(markers.get(sourceFile).isEmpty());

        } finally {
            PMDPlugin.getDefault().getPreferencesManager().loadPreferences().setProjectBuildPathEnabled(oldSetting);
        }
    }

    /**
     * https://github.com/pmd/pmd-eclipse-plugin/issues/96
     */
    @Test
    public void testProjectBuildPathOutsideWorkspace() throws Exception {
        String projectName = "PMDTestProject2";
        Path tempDir = Files.createTempDirectory(projectName);
        boolean oldSetting = PMDPlugin.getDefault().getPreferencesManager().loadPreferences()
                .isProjectBuildPathEnabled();
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IProject newProject = root.getProject(projectName);

        try {
            final IProjectDescription description = newProject.getWorkspace().newProjectDescription(projectName);
            if (!newProject.exists()) {
                description.setLocationURI(tempDir.toUri());
                newProject.create(description, null);
            }

            if (!newProject.isOpen()) {
                newProject.open(null);
            }
            EclipseUtils.addJavaNature(newProject);

            IProjectProperties properties = PMDPlugin.getDefault().getPropertiesManager()
                    .loadProjectProperties(newProject);
            properties.setPmdEnabled(true);
            Rule missingOVerrideRule = properties.getProjectRuleSet().getRuleByName("MissingOverride");
            RuleSet projectRuleSet = RuleSetUtil.newSingle(missingOVerrideRule);
            properties.setProjectRuleSet(projectRuleSet);

            PMDPlugin.getDefault().getPreferencesManager().loadPreferences().setProjectBuildPathEnabled(true);
            IFile sourceFile = createMissingOverrideTestCase(newProject);

            ReviewCodeCmd cmd = new ReviewCodeCmd();
            cmd.addResource(newProject);
            cmd.performExecute();
            cmd.join();
            Map<IFile, Set<MarkerInfo2>> markers = cmd.getMarkers();
            // with type resolution, we detect missing override annotation
            Assert.assertFalse("Type Resolution didn't work", markers.get(sourceFile).isEmpty());

            // without type resolution, there is no violation
            PMDPlugin.getDefault().getPreferencesManager().loadPreferences().setProjectBuildPathEnabled(false);
            cmd = new ReviewCodeCmd();
            cmd.addResource(newProject);
            cmd.performExecute();
            cmd.join();
            markers = cmd.getMarkers();
            // there is no violation expected without type resolution
            Assert.assertTrue(markers.get(sourceFile).isEmpty());

        } finally {
            PMDPlugin.getDefault().getPreferencesManager().loadPreferences().setProjectBuildPathEnabled(oldSetting);

            newProject.delete(true, true, null);
        }
    }

    /**
     * The ReviewCodeCmd must also work on a ResourceDelta
     */
    @Test
    public void testReviewCmdDelta() {
        // Don't know how to test that yet
        // How to instantiate a ResourceDelta ?
        // Let's comment for now
    }

    /**
     * Normally a null resource and a null resource delta is not acceptable.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testReviewCmdNullResource() {
        final ReviewCodeCmd cmd = new ReviewCodeCmd();
        cmd.addResource(null);
        cmd.setResourceDelta(null);
        cmd.performExecute();
    }

    private static Rule findRuleByName(RuleSet ruleset, String ruleName, String language) {
        for (Rule rule : ruleset.getRules()) {
            if (rule.getName().equals(ruleName) && rule.getLanguage().getId().equals(language)) {
                return rule;
            }
        }
        return null;
    }

    @Test
    public void testReviewCmdBasicAllFilesDefault() throws Exception {
        // add a second file
        EclipseUtils.createTestSourceFile(testProject, "/src/Test.js", "function(arg) { notDeclaredVariable = 1; }");
        testProject.refreshLocal(IResource.DEPTH_INFINITE, null);

        IProjectProperties projectProperties = PMDPlugin.getDefault().getPropertiesManager().loadProjectProperties(testProject);
        RuleSet projectRuleSet = projectProperties.getProjectRuleSet();
        Rule emptyCatchBlock = findRuleByName(projectRuleSet, "EmptyCatchBlock", "java");
        projectRuleSet = RuleSetUtil.clearRules(projectRuleSet);
        projectRuleSet = RuleSetUtil.addRule(projectRuleSet, emptyCatchBlock);
        projectProperties.setProjectRuleSet(projectRuleSet);
        PMDPlugin.getDefault().getPropertiesManager().storeProjectProperties(projectProperties);

        final ReviewCodeCmd cmd = new ReviewCodeCmd();
        cmd.addResource(this.testProject);
        cmd.performExecute();
        cmd.join();

        Assert.assertEquals(1, cmd.getFileCount()); // only one file analyzed
    }

    @Test
    public void testReviewCmdBasicAllFiles() throws Exception {
        try {
            // add a second file
            EclipseUtils.createTestSourceFile(testProject, "/src/Test.js", "function(arg) { notDeclaredVariable = 1; }");
            testProject.refreshLocal(IResource.DEPTH_INFINITE, null);

            PMDPlugin.getDefault().loadPreferences().setDetermineFiletypesAutomatically(false);

            IProjectProperties projectProperties = PMDPlugin.getDefault().getPropertiesManager().loadProjectProperties(testProject);
            RuleSet projectRuleSet = projectProperties.getProjectRuleSet();
            Rule emptyCatchBlock = findRuleByName(projectRuleSet, "EmptyCatchBlock", "java");
            projectRuleSet = RuleSetUtil.clearRules(projectRuleSet);
            projectRuleSet = RuleSetUtil.addRule(projectRuleSet, emptyCatchBlock);
            projectProperties.setProjectRuleSet(projectRuleSet);
            PMDPlugin.getDefault().getPropertiesManager().storeProjectProperties(projectProperties);

            final ReviewCodeCmd cmd = new ReviewCodeCmd();
            cmd.addResource(this.testProject);
            cmd.performExecute();
            cmd.join();

            Assert.assertEquals(2, cmd.getFileCount()); // both files analyzed, although unnecessary, since only
            // the one java rule is active and no javascript rules
        } finally {
            PMDPlugin.getDefault().loadPreferences().setDetermineFiletypesAutomatically(IPreferences.DETERMINE_FILETYPES_AUTOMATICALLY_DEFAULT);
        }
    }
}
