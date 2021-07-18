/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.actions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;

import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 * Deletes selected Violations Adapted from Phillipe Herlin.
 * 
 * @author SebastianRaffel ( 21.05.2005 )
 */
public class RemoveViolationAction extends AbstractViolationSelectionAction {

    public RemoveViolationAction(TableViewer viewer) {
        super(viewer);
    }

    @Override
    protected String textId() {
        return StringKeys.VIEW_ACTION_REMOVE_VIOLATION;
    }

    @Override
    protected String imageId() {
        return PMDUiConstants.ICON_BUTTON_REMVIO;
    }

    @Override
    protected String tooltipMsgId() {
        return StringKeys.VIEW_TOOLTIP_REMOVE_VIOLATION;
    }

    /**
     * Executes the Action.
     */
    @Override
    public void run() {
        // simply: get all Markers
        final IMarker[] markers = getSelectedViolations();
        if (markers == null) {
            return;
        }

        try {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            workspace.run(new IWorkspaceRunnable() {
                @Override
                public void run(IProgressMonitor monitor) throws CoreException {
                    for (IMarker marker : markers) {
                        marker.delete(); // ... and delete them
                    }
                }
            }, null);
        } catch (CoreException ce) {
            logErrorByKey(StringKeys.ERROR_CORE_EXCEPTION, ce);
        }
    }
}
