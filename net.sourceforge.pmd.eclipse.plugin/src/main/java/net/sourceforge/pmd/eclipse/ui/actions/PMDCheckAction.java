/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.cmd.AbstractDefaultCommand;
import net.sourceforge.pmd.eclipse.runtime.cmd.ReviewCodeCmd;
import net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 * Implements action on the "Check code with PMD" action menu on a file
 *
 * @author Philippe Herlin
 *
 */
public class PMDCheckAction extends AbstractUIAction {

    private static final Logger LOG = LoggerFactory.getLogger(PMDCheckAction.class);

    /**
     * @see org.eclipse.ui.IActionDelegate#run(IAction)
     */
    public void run(IAction action) {
        LOG.info("Check PMD action requested");

        try {

            // Execute PMD on a range of selected resource if action selected
            // from a view part
            if (isViewPart()) {
                ISelection selection = targetSelection();
                if (selection instanceof IStructuredSelection) {
                    reviewSelectedResources((IStructuredSelection) selection);
                } else {
                    LOG.debug("The selection is not an instance of IStructuredSelection. This is not supported: "
                            + selection.getClass().getName());
                }
            } else if (isEditorPart()) {
                // If action is selected from an editor, run PMD on the file
                // currently edited
                IEditorInput editorInput = ((IEditorPart) targetPart()).getEditorInput();
                if (editorInput instanceof IFileEditorInput) {
                    reviewSingleResource(((IFileEditorInput) editorInput).getFile());
                } else {
                    LOG.debug("The kind of editor input is not supported. The editor input if of type: "
                            + editorInput.getClass().getName());
                }
            } else {
                // Else, this is not supported for now
                LOG.debug("Running PMD from this kind of part is not supported. Part is of type "
                        + targetPartClassName());
            }

        } catch (RuntimeException e) {
            showErrorById(StringKeys.ERROR_CORE_EXCEPTION, e);
        }

    }

    /**
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * Run the reviewCode command on a single resource
     *
     * @param resource
     */
    private void reviewSingleResource(IResource resource) {
        ReviewCodeCmd cmd = new ReviewCodeCmd();
        cmd.addResource(resource);

        setupAndExecute(cmd, 1);
    }

    private void setupAndExecute(ReviewCodeCmd cmd, int count) {
        cmd.setStepCount(count);
        cmd.setTaskMarker(true);
        cmd.setOpenPmdPerspective(PMDPlugin.getDefault().loadPreferences().isPmdPerspectiveEnabled());
        cmd.setOpenPmdViolationsOverviewView(PMDPlugin.getDefault().loadPreferences().isPmdViolationsOverviewEnabled());
        cmd.setOpenPmdViolationsOutlineView(PMDPlugin.getDefault().loadPreferences().isPmdViolationsOutlineEnabled());
        cmd.setUserInitiated(true);
        cmd.setRunAlways(true);
        cmd.performExecute();
    }

    /**
     * Prepare and run the reviewCode command for all selected resources
     *
     * @param selection
     *            the selected resources
     */
    private void reviewSelectedResources(IStructuredSelection selection) {
        ReviewCodeCmd cmd = new ReviewCodeCmd();

        // Add selected resources to the list of resources to be reviewed
        for (Iterator<?> i = selection.iterator(); i.hasNext();) {
            Object element = i.next();
            if (element instanceof AbstractPMDRecord) {
                IResource resource = ((AbstractPMDRecord) element).getResource();
                if (resource != null) {
                    cmd.addResource(resource);
                } else {
                    LOG.warn("The selected object has no resource");
                    LOG.debug("  -> selected object : " + element);
                }
            } else if (element instanceof IWorkingSet) {
                IWorkingSet set = (IWorkingSet) element;
                for (IAdaptable adaptable : set.getElements()) {
                    addAdaptable(cmd, adaptable);
                }
            } else if (element instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) element;
                addAdaptable(cmd, adaptable);
            } else {
                LOG.warn("The selected object is not adaptable");
                LOG.debug("   -> selected object : " + element);
            }
        }

        // Run the command
        setupAndExecute(cmd, countElements(selection));
    }

    private void addAdaptable(ReviewCodeCmd cmd, IAdaptable adaptable) {
        IResource resource = (IResource) adaptable.getAdapter(IResource.class);
        if (resource != null) {
            cmd.addResource(resource);
        } else {
            LOG.warn("The selected object cannot adapt to a resource");
            LOG.debug("   -> selected object : " + adaptable);
        }
    }

    /**
     * Count the number of resources of a selection
     *
     * @param selection
     *            a selection
     * @return the element count
     */
    private int countElements(IStructuredSelection selection) {
        CountVisitor visitor = new CountVisitor();

        for (Iterator<?> i = selection.iterator(); i.hasNext();) {
            Object element = i.next();

            try {
                if (element instanceof IAdaptable) {
                    IAdaptable adaptable = (IAdaptable) element;
                    IResource resource = (IResource) adaptable.getAdapter(IResource.class);
                    if (resource != null) {
                        resource.accept(visitor);
                    } else {
                        LOG.warn("The selected object cannot adapt to a resource");
                        LOG.debug("   -> selected object : " + element);
                    }
                } else {
                    LOG.warn("The selected object is not adaptable");
                    LOG.debug("   -> selected object : " + element);
                }
            } catch (CoreException e) {
                // Ignore any exception
                logError("Exception when counting the number of impacted elements when running PMD from menu", e);
            }
        }

        return visitor.count;
    }

    // Inner visitor to count number of children of a resource
    private class CountVisitor implements IResourceVisitor {
        public int count = 0;

        public boolean visit(IResource resource) {
            boolean fVisitChildren = true;
            count++;

            if (resource instanceof IFile && AbstractDefaultCommand.isJavaFile((IFile) resource)) {

                fVisitChildren = false;
            }

            return fVisitChildren;
        }
    }
}
