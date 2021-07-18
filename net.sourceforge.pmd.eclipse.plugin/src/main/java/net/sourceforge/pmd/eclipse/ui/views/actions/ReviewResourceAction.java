/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.pmd.eclipse.runtime.cmd.ReviewCodeCmd;
import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 * Action for reviewing one single resource.
 * 
 * @author Sven Jacob
 *
 */
public class ReviewResourceAction extends AbstractPMDAction {

    private IProgressMonitor monitor;
    private IResource resource;

    public ReviewResourceAction(IResource resource) {
        super();
        this.resource = resource;
    }

    @Override
    protected String imageId() {
        return PMDUiConstants.ICON_BUTTON_REFRESH;
    }

    @Override
    protected String tooltipMsgId() {
        return StringKeys.VIEW_TOOLTIP_REFRESH;
    }

    public void setResource(IResource resource) {
        this.resource = resource;
    }

    @Override
    public void run() {
        try {
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
            dialog.run(false, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    setMonitor(monitor);
                    monitor.beginTask(getString(StringKeys.MONITOR_REVIEW), 5);
                    ReviewCodeCmd cmd = new ReviewCodeCmd();
                    cmd.addResource(resource);
                    cmd.setStepCount(1);
                    cmd.setTaskMarker(true);
                    cmd.setUserInitiated(true);
                    try {
                        cmd.performExecute();
                    } catch (RuntimeException e) {
                        logErrorByKey(StringKeys.ERROR_CORE_EXCEPTION, e);
                    }
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e) {
            logErrorByKey(StringKeys.ERROR_INVOCATIONTARGET_EXCEPTION, e);
        } catch (InterruptedException e) {
            logErrorByKey(StringKeys.ERROR_INTERRUPTED_EXCEPTION, e);
        }
    }

    protected IProgressMonitor getMonitor() {
        return monitor;
    }

    protected void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }
}
