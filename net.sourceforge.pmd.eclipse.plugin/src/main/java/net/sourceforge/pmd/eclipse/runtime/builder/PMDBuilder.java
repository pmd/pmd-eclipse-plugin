/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.cmd.ReviewCodeCmd;

/**
 * Implements an incremental builder for PMD. Use ResourceVisitor and
 * DeltaVisitor to process each file of the project.
 *
 * @author Philippe Herlin
 *
 */
public class PMDBuilder extends IncrementalProjectBuilder {

    public static final Logger LOG = LoggerFactory.getLogger(PMDBuilder.class);
    public static final String PMD_BUILDER = "net.sourceforge.pmd.eclipse.plugin.pmdBuilder";

    public static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];

    /**
     * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int,
     *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
        IProject currentProject = this.getProject();
        LOG.debug("Incremental builder activated for {}", currentProject);

        try {
            if (kind == AUTO_BUILD) {
                LOG.debug("Auto build requested.");
                buildIncremental(currentProject, monitor);
            } else if (kind == FULL_BUILD) {
                LOG.debug("Full build requested.");
                buildFull(currentProject, monitor);
            } else if (kind == INCREMENTAL_BUILD) {
                LOG.debug("Incremental build requested.");
                buildIncremental(currentProject, monitor);
            } else {
                LOG.warn("Ignoring IncrementalBuilder request of kind {} for {} - not supported", kind, currentProject);
            }
        } catch (RuntimeException e) {
            throw new CoreException(new Status(IStatus.ERROR, PMDPlugin.getDefault().getBundle().getSymbolicName(), 0,
                    e.getMessage(), e));
        }

        return EMPTY_PROJECT_ARRAY;
    }

    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        MarkerUtil.deleteAllMarkersIn(getProject());
    }

    /**
     * Full build
     * 
     * @param monitor
     *            A progress monitor.
     */
    private void buildFull(IProject project, IProgressMonitor monitor) {
        this.processProjectFiles(project, monitor);
    }

    /**
     * Incremental build
     * 
     * @param monitor
     *            a progress monitor.
     */
    private void buildIncremental(IProject project, IProgressMonitor monitor) {
        // Check the user preference to see if the user wants to run PMD on a save
        // If the preference "Check code after saving" is NOT enabled, then we don't
        // execute this incremental build request.
        // Note: Full Build Requests are done regardless of the "Check code after saving" preference.
        if (!PMDPlugin.getDefault().loadPreferences().isCheckAfterSaveEnabled()) {
            return;
        }

        IResourceDelta resourceDelta = this.getDelta(project);
        if (resourceDelta != null && resourceDelta.getAffectedChildren().length != 0) {
            ReviewCodeCmd cmd = new ReviewCodeCmd();
            cmd.setResourceDelta(resourceDelta);
            cmd.setMonitor(monitor);
            // a builder is always asynchronous;
            // execute a command synchronously
            // whatever its processor
            cmd.performExecute();
        } else {
            LOG.debug("No change reported. Performing no build");
        }
    }

    /**
     * Process all files in the project.
     * 
     * @param project the project
     * @param monitor a progress monitor
     */
    private void processProjectFiles(IProject project, IProgressMonitor monitor) {
        ReviewCodeCmd cmd = new ReviewCodeCmd();
        cmd.addResource(project);
        cmd.setMonitor(monitor);
        // a builder is always asynchronous; execute a command synchronously whatever its processor
        cmd.performExecute(); 
    }

}
