/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.views.ViolationOverview;

/**
 * 
 * @author Brian Remedios
 */
public abstract class AbstractUIAction implements IObjectActionDelegate {

    private ISelection selection;
    private IWorkbenchPart targetPart;

    protected AbstractUIAction() {
        // only allow subclassing
    }

    protected IWorkbenchPart targetPart() {
        return targetPart;
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart theTargetPart) {
        targetPart = theTargetPart;
    }

    protected boolean isViewPart() {
        return targetPart instanceof IViewPart;
    }

    protected boolean isEditorPart() {
        return targetPart instanceof IEditorPart;
    }

    protected boolean isViolationOverview() {
        return targetPart instanceof ViolationOverview;
    }

    protected String targetPartClassName() {
        return targetPart.getClass().getName();
    }

    protected IWorkbenchPartSite targetPartSite() {
        return targetPart.getSite();
    }

    protected ISelection targetSelection() {
        return selection;
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

    /**
     * Helper method to return an NLS string from its key
     */
    protected String getString(String key) {
        return PMDPlugin.getDefault().getStringTable().getString(key);
    }

    protected void showErrorById(String errorId, Throwable th) {
        PMDPlugin.getDefault().showError(getString(errorId), th);
    }

    protected void logError(String message, Throwable error) {
        PMDPlugin.getDefault().logError(message, error);
    }
}
