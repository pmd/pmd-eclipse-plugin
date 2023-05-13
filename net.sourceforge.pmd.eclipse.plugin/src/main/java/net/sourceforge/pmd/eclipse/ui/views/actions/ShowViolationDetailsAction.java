/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.ui.views.actions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Shell;

import net.sourceforge.pmd.eclipse.ui.dialogs.ViolationDetailsDialog;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

public class ShowViolationDetailsAction extends AbstractViolationSelectionAction {
    private final Shell shell;

    public ShowViolationDetailsAction(Shell shell, TableViewer viewer) {
        super(viewer);
        this.shell = shell;
    }

    @Override
    protected String textId() {
        return StringKeys.VIEW_ACTION_SHOW_RULE;
    }

    @Override
    protected String imageId() {
        return null;
    }

    @Override
    protected String tooltipMsgId() {
        return StringKeys.VIEW_TOOLTIP_SHOW_RULE;
    }

    @Override
    protected boolean canExecute() {
        return super.canExecute() && hasSingleSelection();
    }

    private boolean hasSingleSelection() {
        IMarker[] markers = getSelectedViolations();
        return markers.length == 1;
    }

    @Override
    public void run() {
        ViolationDetailsDialog dialog = new ViolationDetailsDialog(shell, getSelectedViolations()[0]);
        dialog.open();
    }
}
