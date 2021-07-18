/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 *
 * @author SebastianRaffel ( 26.05.2005 )
 */
public class ShowDataflowAction implements IObjectActionDelegate {
    private IWorkbenchPage workbenchPage;

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.workbenchPage = targetPart.getSite().getPage();
    }

    @Override
    public void run(IAction action) {
        if (this.workbenchPage != null) {
            try {
                this.workbenchPage.showView(PMDUiConstants.ID_DATAFLOWVIEW);

            } catch (PartInitException pie) {
                PMDPlugin.getDefault().logError(StringKeys.ERROR_VIEW_EXCEPTION + this.toString(), pie);
            }
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(true);
    }
}
