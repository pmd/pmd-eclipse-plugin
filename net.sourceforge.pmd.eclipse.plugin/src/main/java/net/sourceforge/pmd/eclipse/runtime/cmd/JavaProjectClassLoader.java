/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.core.internal.FileModificationUtil;

/**
 * This is a ClassLoader for the Build Path of an IJavaProject.
 */
public class JavaProjectClassLoader extends URLClassLoader {
    private static final Logger LOG = LoggerFactory.getLogger(JavaProjectClassLoader.class);

    private final IJavaProject javaProject;
    private final long lastModTimestamp;
    private final IWorkspace workspace;
    private Set<IJavaProject> javaProjects = new HashSet<IJavaProject>();


    public JavaProjectClassLoader(ClassLoader parent, IProject project) {
        super(new URL[0], parent);
        try {
            if (!project.hasNature(JavaCore.NATURE_ID)) {
                throw new IllegalArgumentException("The project " + project + " is not a java project");
            }
        } catch (CoreException e) {
            throw new IllegalArgumentException("The project " + project + " is not a java project", e);
        }

        workspace = project.getWorkspace();
        javaProject = JavaCore.create(project);
        lastModTimestamp = getClasspathModificationTimestamp();
        addURLs(javaProject, false);

        // No longer need these things, drop references
        javaProjects = null;
    }

    public boolean isModified() {
        long newTimestamp = getClasspathModificationTimestamp();
        return newTimestamp != lastModTimestamp;
    }

    private long getClasspathModificationTimestamp() {
        IFile classpathFile = javaProject.getProject().getFile(IJavaProject.CLASSPATH_FILE_NAME);
        return FileModificationUtil.getFileModificationTimestamp(classpathFile.getLocation().toFile());
    }

    private IProject projectFor(IClasspathEntry classpathEntry) {
        return workspace.getRoot().getProject(classpathEntry.getPath().toString());
    }

    private void addURLs(IJavaProject javaProject, boolean exportsOnly) {

        if (javaProjects.contains(javaProject)) {
            return;
        }

        javaProjects.add(javaProject);

        try {
            // Add default output location
            IPath projectLocation = javaProject.getProject().getLocation();
            addURL(projectLocation.append(javaProject.getOutputLocation().removeFirstSegments(1)));

            // Add each classpath entry
            IClasspathEntry[] classpathEntries = javaProject.getResolvedClasspath(true);
            for (IClasspathEntry classpathEntry : classpathEntries) {
                if (classpathEntry.isExported() || !exportsOnly) {
                    switch (classpathEntry.getEntryKind()) {

                    // Recurse on projects
                    case IClasspathEntry.CPE_PROJECT:
                        IProject project = projectFor(classpathEntry);
                        IJavaProject javaProj = JavaCore.create(project);
                        if (javaProj != null) {
                            addURLs(javaProj, true);
                        }
                        break;

                    // Library
                    case IClasspathEntry.CPE_LIBRARY:
                        addURL(classpathEntry);
                        break;

                    // Only Source entries with custom output location need to
                    // be added
                    case IClasspathEntry.CPE_SOURCE:
                        IPath outputLocation = classpathEntry.getOutputLocation();
                        if (outputLocation != null) {
                            addURL(projectLocation.append(outputLocation.removeFirstSegments(1)));
                        }
                        break;

                    // Variable and Container entries should not be happening,
                    // because we've asked for resolved entries.
                    case IClasspathEntry.CPE_VARIABLE:
                    case IClasspathEntry.CPE_CONTAINER:
                    default:
                        break;
                    }
                }
            }
        } catch (JavaModelException e) {
            LOG.warn("JavaModelException occurred: {}", e.getMessage(), e);
        }
    }

    private void addURL(IClasspathEntry classpathEntry) {
        addURL(classpathEntry.getPath());
    }

    private void addURL(IPath path) {
        try {
            File absoluteFile = null;
            IPath location = workspace.getRoot().getFile(path).getLocation();
            if (location != null) {
                // location is only present, if a project exists in the workspace
                // in other words: only if path referenced something inside an existing project
                absoluteFile = location.toFile().getAbsoluteFile();
            }

            if (absoluteFile == null) {
                // if location couldn't be resolved, then it is already an absolute path
                absoluteFile = path.toFile().getAbsoluteFile();
            }

            if (!absoluteFile.exists()) {
                LOG.warn("auxclasspath: Resolved file {} does not exist", absoluteFile);
            }
            URL url = absoluteFile.toURI().toURL();
            LOG.debug("auxclasspath: Adding url {}", url);
            addURL(url);
        } catch (MalformedURLException e) {
            LOG.warn("MalformedURLException occurred: {}", e.getMessage(), e);
        }
    }
}
