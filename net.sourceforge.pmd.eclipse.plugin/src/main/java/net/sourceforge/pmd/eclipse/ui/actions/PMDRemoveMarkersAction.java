/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.runtime.builder.MarkerUtil;
import net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.views.ViolationOverview;

/**
 * Process "Delete PMD Markers" action menu
 * 
 * @author phherlin
 * 
 */
public class PMDRemoveMarkersAction extends AbstractUIAction implements IViewActionDelegate {

    private static final String VIEW_ACTION = "net.sourceforge.pmd.eclipse.ui.pmdRemoveAllMarkersAction";
    private static final String OBJECT_ACTION = "net.sourceforge.pmd.eclipse.ui.pmdRemoveMarkersAction";
    private static final Logger LOG = LoggerFactory.getLogger(PMDRemoveMarkersAction.class);

    @Override
    public void init(IViewPart view) {
        setActivePart(null, view);
    }

    @Override
    public void run(IAction action) {
        LOG.info("Remove Markers action requested");
        try {
            if (VIEW_ACTION.equals(action.getId())) {
                final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                MarkerUtil.deleteAllMarkersIn(root);
                LOG.debug("Remove markers over the whole workspace");
            } else if (OBJECT_ACTION.equals(action.getId())) {
                processResource();
            } else { // else action id not supported
                LOG.warn("Cannot remove markers, action ID is not supported");
            }
        } catch (CoreException e) {
            showErrorById(StringKeys.ERROR_CORE_EXCEPTION, e);
        }
    }

    /**
     * Process removing of makers on a resource selection (project or file)
     */
    private void processResource() {
        LOG.debug("Processing a resource");
        try {
            // if action is run from a view, process the selected resources
            final ISelection sel = targetSelection();

            if (sel instanceof IStructuredSelection) {
                final IStructuredSelection structuredSel = (IStructuredSelection) sel;
                for (final Iterator<?> i = structuredSel.iterator(); i.hasNext();) {
                    final Object element = i.next();
                    processElement(element);
                }
            } else {
                LOG.warn("The view part selection is not a structured selection !");
            }
        } catch (CoreException e) {
            showErrorById(StringKeys.ERROR_CORE_EXCEPTION, e);
        }
    }

    private void processElement(Object element) throws CoreException {
        if (element instanceof AbstractPMDRecord) {
            final AbstractPMDRecord record = (AbstractPMDRecord) element;
            final IResource resource = record.getResource();
            if (isViolationOverview()) {
                ((ViolationOverview) targetPart()).deleteMarkers(record);
            }

            LOG.debug("Remove markers on resource " + resource.getName());
        } else if (element instanceof IAdaptable) {
            final IAdaptable adaptable = (IAdaptable) element;
            final IResource resource = (IResource) adaptable.getAdapter(IResource.class);
            if (resource == null) {
                LOG.warn("The selected object cannot adapt to a resource");
                LOG.debug("   -> selected object : " + element);
            } else {
                MarkerUtil.deleteAllMarkersIn(resource);
                LOG.debug("Remove markers on resrouce " + resource.getName());
            }
        } else {
            LOG.warn("The selected object is not adaptable");
            LOG.debug("   -> selected object : " + element);
        }
    }

}
