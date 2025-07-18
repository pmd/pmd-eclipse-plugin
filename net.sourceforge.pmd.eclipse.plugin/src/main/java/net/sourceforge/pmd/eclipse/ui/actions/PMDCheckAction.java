/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
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
     * 
     */
    public PMDCheckAction() {
        LOG.info("New Check Action created...");
    }
    
    @Override
    public void run(IAction action) {
        LOG.info("Check PMD action requested");

        try {
            ISelection selection = targetSelection();
            if (selection instanceof IStructuredSelection) {
                reviewSelectedResources((IStructuredSelection) selection);
            } else {
                LOG.debug("The selection is not an instance of IStructuredSelection. This is not supported: "
                        + selection.getClass().getName());
            }
        } catch (RuntimeException e) {
            showErrorById(StringKeys.ERROR_CORE_EXCEPTION, e);
        }

    }

    private void setupAndExecute(ReviewCodeCmd cmd) {
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
        setupAndExecute(cmd);
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
}
