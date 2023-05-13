/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.ui.views.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.pmd.eclipse.ui.dialogs.ViolationDetailsDialog;

public class ShowViolationDetailsHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        IStructuredSelection currentSelection = HandlerUtil.getCurrentStructuredSelection(event);
        if (currentSelection.size() == 1) {
            Object first = currentSelection.getFirstElement();
            if (first instanceof IAdaptable) {
                IMarker marker = ((IAdaptable) first).getAdapter(IMarker.class);
                if (marker != null) {
                    ViolationDetailsDialog dialog = new ViolationDetailsDialog(shell, marker);
                    dialog.open();
                }
            }
        }
        return null;
    }

}
