/**
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
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

    /**
     * @see org.eclipse.ui.IViewActionDelegate#init(IViewPart)
     */
    public void init(IViewPart view) {
        // no initialization for now
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#run(IAction)
     */
    public void run(IAction action) {
        LOG.info("Remove Markers action requested");
        try {
            if (action.getId().equals(VIEW_ACTION)) {
                final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                MarkerUtil.deleteAllMarkersIn(root);
                LOG.debug("Remove markers over the whole workspace");
            } else if (action.getId().equals(OBJECT_ACTION)) {
                processResource();
            } else { // else action id not supported
                LOG.warn("Cannot remove markers, action ID is not supported");
            }
        } catch (CoreException e) {
            showErrorById(StringKeys.ERROR_CORE_EXCEPTION, e);
        }
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // nothing to do
    }

    /**
     * Process removing of makers on a resource selection (project or file)
     */
    private void processResource() {
        LOG.debug("Processing a resource");
        try {
            if (isViewPart()) {
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
            } else if (isEditorPart()) {
                // if action is run from an editor, process the file currently
                // edited
                final IEditorInput editorInput = ((IEditorPart) targetPart()).getEditorInput();
                if (editorInput instanceof IFileEditorInput) {
                    MarkerUtil.deleteAllMarkersIn(((IFileEditorInput) editorInput).getFile());
                    LOG.debug("Remove markers on currently edited file "
                            + ((IFileEditorInput) editorInput).getFile().getName());
                } else {
                    LOG.debug("The kind of editor input is not supported. The editor input type: "
                            + editorInput.getClass().getName());
                }
            } else {
                // else, this is not supported
                LOG.debug("This action is not supported on that part. This part type is: " + targetPartClassName());
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
