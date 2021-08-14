/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.junit.Assert;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.builder.PMDNature;
import net.sourceforge.pmd.properties.PropertyDescriptor;

/**
 * This is a utility class for Eclipse various operations
 *
 * @author Philippe Herlin
 * @author Brian Remedios
 */
public class EclipseUtils {
    static class OpenMonitor extends NullProgressMonitor {
        private final CountDownLatch latch;

        OpenMonitor(final CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void done() {
            super.done();
            latch.countDown();
        }
    }

    /**
     * Because this class is a utility class, it cannot be instantiated
     */
    private EclipseUtils() {
        super();
    }

    /**
     * Test if 2 sets of rules are equals
     *
     * @param ruleSet1
     * @param ruleSet2
     * @return
     */
    public static boolean assertRuleSetEquals(final Collection<Rule> ruleSet1, final Collection<Rule> ruleSet2,
            final PrintStream out) {
        boolean equals = true;

        for (final Iterator<Rule> i = ruleSet1.iterator(); i.hasNext() && equals;) {
            final Rule rule = i.next();
            if (!searchRule(rule, ruleSet2, out)) {
                equals = false;
                System.out.println("Rule " + rule.getName() + " is not in the second ruleset");
            }
        }

        for (final Iterator<Rule> i = ruleSet2.iterator(); i.hasNext() && equals;) {
            final Rule rule = i.next();
            if (!searchRule(rule, ruleSet1, out)) {
                equals = false;
                System.out.println("Rule " + rule.getName() + " is not in the first ruleset");
            }
        }

        return equals;
    }

    /**
     * Create a new java project
     *
     * @param projectName
     *            a project name
     * @return newProject a new project resource handle
     */
    public static IProject createJavaProject(final String projectName) throws CoreException {
        final IProject newProject = createProject(projectName);

        // 4. Make it a Java Project
        addJavaNature(newProject);

        return newProject;
    }

    public static IProject createProject(final String projectName) throws CoreException {
        // 1. Get the project from the workspace
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IProject newProject = root.getProject(projectName);
        final IProjectDescription description = newProject.getWorkspace().newProjectDescription(projectName);

        // 2. Create a project if it does not already exist
        if (!newProject.exists()) {
            description.setLocation(null);
            newProject.create(description, null);
        }

        if (!newProject.isOpen()) {
            newProject.open(null);
        }

        return newProject;
    }

    /**
     * Create a test source file
     *
     * @param project
     *            a project where to create that file; this project is expected to be empty
     */
    public static IFile createTestSourceFile(final IProject project)
            throws JavaModelException, CoreException, IOException {

        // 1. Locate the test java source template
        try (InputStream is = EclipseUtils.class.getResourceAsStream("/test.template")) {
            // 2. Copy the template inside the source directory
            final IFile sourceFile = project.getFile("/src/Test.java");
            if (sourceFile.exists() && sourceFile.isAccessible()) {
                sourceFile.setContents(is, true, false, null);
            } else {
                sourceFile.create(is, true, null);
            }
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
            
            return sourceFile;
        }
    }

    /**
     * Get the content of a project resource.
     *
     * @param project
     *            a project reference
     * @param resourceName
     *            the name of the resource (@see IProject)
     * @return the resource content as an InputStream or null
     * @throws CoreException
     */
    public static InputStream getResourceStream(final IProject project, final String resourceName)
            throws CoreException {
        final IFile file = project.getFile(resourceName);
        return file != null && file.exists() && file.isAccessible() ? file.getContents(true) : null;
    }

    /**
     * Remove the PMD Nature from a project
     *
     * @param project
     *            a project to remove the PMD Nature
     * @param monitor
     *            a progress monitor
     * @return success true if the nature has been removed; false means the project already had not the PMD Nature.
     * @throws CoreException
     *             if any error occurs.
     */
    public static boolean removePMDNature(final IProject project) throws CoreException {
        final boolean success = false;

        if (project.hasNature(PMDNature.PMD_NATURE)) {
            final IProjectDescription description = project.getDescription();
            final String[] natureIds = description.getNatureIds();
            final String[] newNatureIds = new String[natureIds.length - 1];
            for (int i = 0, j = 0; i < natureIds.length; i++) {
                if (!PMDNature.PMD_NATURE.equals(natureIds[i])) {
                    newNatureIds[j++] = natureIds[i];
                }
            }
            description.setNatureIds(newNatureIds);
            project.setDescription(description, null);
            project.deleteMarkers(PMDRuntimeConstants.PMD_MARKER, true, IResource.DEPTH_INFINITE);

            final IFile file = project.getFile(".pmd");
            if (file.exists() && file.isAccessible()) {
                file.delete(true, false, null);
            }
        }

        return success;
    }

    /**
     * Add a Java Nature to a project when creating
     *
     * @param project
     * @throws CoreException
     */
    public static void addJavaNature(final IProject project) throws CoreException {
        if (!project.hasNature(JavaCore.NATURE_ID)) {
            final IProjectDescription description = project.getDescription();
            final String[] prevNatures = description.getNatureIds();
            final String[] newNatures = new String[prevNatures.length + 1];
            System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
            newNatures[prevNatures.length] = JavaCore.NATURE_ID;
            description.setNatureIds(newNatures);
            project.setDescription(description, null);
            IFolder sourceFolder = project.getFolder("/src");
            sourceFolder.create(true, true, null);

            IJavaProject javaProject = JavaCore.create(project);
            javaProject
                    .setRawClasspath(
                            new IClasspathEntry[] { JavaCore.newSourceEntry(sourceFolder.getFullPath()),
                                JavaRuntime.getDefaultJREContainerEntry() },
                            null);

            Hashtable<String, String> javaOptions = JavaCore.getOptions();
            javaOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
            javaOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
            javaOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
            javaProject.setOptions(javaOptions);
        }
    }

    /**
     * Print rule details
     *
     * @param rule
     */
    private static void dumpRule(final Rule rule, final PrintStream out) {
        out.println("Rule: " + rule.getName());
        out.println("Priority: " + rule.getPriority());
        final Map<PropertyDescriptor<?>, Object> properties = rule.getPropertiesByPropertyDescriptor();
        final Set<Entry<PropertyDescriptor<?>, Object>> keys = properties.entrySet();
        for (final Entry<PropertyDescriptor<?>, Object> entry : keys) {
            out.println("   " + entry.getKey().name() + " = " + entry.getValue());
        }
    }

    private static boolean propertiesMatchFor(final Rule ruleA, final Rule ruleB) {
        Map<PropertyDescriptor<?>, Object> ruleAProperties = ruleA.getPropertiesByPropertyDescriptor();

        Map<PropertyDescriptor<?>, Object> ruleBProperties = ruleB.getPropertiesByPropertyDescriptor();

        // simple equals doesn't work for RegexProperties whose value type is java.util.regex.Pattern...
        //return ruleAProperties.equals(ruleBProperties);

        if (ruleAProperties == ruleBProperties) { // NOPMD: CompareObjectsWithEquals is needed here
            return true;
        }

        if (ruleAProperties.size() != ruleBProperties.size()) {
            return false;
        }

        for (Map.Entry<PropertyDescriptor<?>, Object> entry : ruleAProperties.entrySet()) {
            if (entry.getValue() == null) {
                Object valueB = ruleBProperties.get(entry.getKey());
                if (!(valueB == null && ruleBProperties.containsKey(entry.getKey()))) {
                    return false;
                }
            } else {
                Object valueB = ruleBProperties.get(entry.getKey());
                if (entry.getValue() instanceof Pattern) {
                    if (!(valueB instanceof Pattern)) {
                        return false;
                    }
                    if (!entry.getValue().toString().equals(valueB.toString())) {
                        return false;
                    }
                } else {
                    if (!entry.getValue().equals(valueB)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Search a rule in a set of rules
     *
     * @param rule
     * @param set
     * @return
     */
    private static boolean searchRule(final Rule rule, final Collection<Rule> set, final PrintStream out) {
        boolean found = false;

        for (final Iterator<Rule> i = set.iterator(); i.hasNext() && !found;) {
            final Rule r = i.next();
            if (r.getClass().getName().equals(rule.getClass().getName())) {
                found = r.getName().equals(rule.getName()) && propertiesMatchFor(r, rule)
                        && r.getPriority() == rule.getPriority();
                if (!found && r.getName().equals(rule.getName())) {
                    out.println("Rules " + r.getName() + " are different because:");
                    out.println("Priorities are different: " + (r.getPriority() != rule.getPriority()));
                    out.println("Properties are different: " + !propertiesMatchFor(r, rule));
                    out.println();
                    out.println("Rule to search");
                    dumpRule(rule, out);
                    out.println();
                    out.println("Rule from set");
                    dumpRule(r, out);
                    out.println();
                }
            }
        }

        return found;
    }

    /**
     * @param testProject
     * @param string
     * @param string2
     * @return
     * @throws CoreException
     * @throws IOException
     */
    public static IFile createTestSourceFile(IProject testProject, String fileName, String content)
            throws CoreException, IOException {
        IFile sourceFile = testProject.getFile(fileName);
        InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
        if (sourceFile.exists() && sourceFile.isAccessible()) {
            sourceFile.setContents(is, true, false, null);
        } else {
            sourceFile.create(is, true, null);
        }
        is.close();
        return sourceFile;
    }

    /**
     * Creates a folder structure recursively.
     *
     * @param testProject
     * @param fullpath
     * @throws CoreException
     */
    public static void createFolders(IProject testProject, String fullpath) throws CoreException {
        IPath path = new Path(fullpath);
        IContainer current = testProject;
        for (String segment : path.segments()) {
            IFolder folder = current.getFolder(new Path(segment));
            if (!folder.exists()) {
                folder.create(true, false, null);
            }
            current = folder;
        }
    }

    public static void waitForJobs() throws InterruptedException {
        long start = System.currentTimeMillis();
        while (!Job.getJobManager().isIdle()) {
            Thread.sleep(500);

            if (System.currentTimeMillis() - start > TimeUnit.SECONDS.toMillis(30)) {
                Assert.fail("Timeout while waiting for Jobs to finish");
            }
        }
    }
}
