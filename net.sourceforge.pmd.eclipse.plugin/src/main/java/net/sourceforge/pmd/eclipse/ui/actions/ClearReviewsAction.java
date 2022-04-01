/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.actions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 * Implements the clear reviews action.
 *
 * @author Philippe Herlin
 *
 */
public class ClearReviewsAction extends AbstractUIAction implements IResourceVisitor, IViewActionDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(ClearReviewsAction.class);
    private IProgressMonitor monitor;

    @Override
    public void init(IViewPart view) {
        setActivePart(null, view.getSite().getPage().getActivePart());
    }

    @Override
    public void run(IAction action) {
        LOG.info("Remove violation reviews requested.");
        ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
        try {
            monitorDialog.run(false, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    setMonitor(monitor);
                    clearReviews();
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e) {
            logError("Invocation Target Exception when removing violation reviews", e.getTargetException());
        } catch (InterruptedException e) {
            logError("Interrupted Exception when removing violation reviews", e);
        }
    }

    /**
     * Get the monitor.
     *
     * @return
     */
    protected IProgressMonitor getMonitor() {
        return monitor;
    }

    /**
     * Set the monitor
     *
     * @param monitor
     */
    protected void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Progress monitor
     */
    protected void monitorWorked() {
        if (getMonitor() != null) {
            getMonitor().worked(1);
        }
    }

    /**
     * Set a substask
     *
     * @param message
     */
    protected void monitorSubTask(String message) {
        if (getMonitor() != null) {
            getMonitor().subTask(message);
        }
    }

    /**
     * Process the clear review action
     */
    protected void clearReviews() {

        try {
            ISelection selection = targetSelection();

            if (selection instanceof IStructuredSelection) {
                IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                if (getMonitor() != null) {
                    getMonitor().beginTask(getString(StringKeys.MONITOR_REMOVE_REVIEWS), IProgressMonitor.UNKNOWN);

                    Iterator<?> i = structuredSelection.iterator();
                    while (i.hasNext()) {
                        Object object = i.next();
                        IResource resource = null;

                        if (object instanceof IMarker) {
                            resource = ((IMarker) object).getResource();
                        } else if (object instanceof IAdaptable) {
                            IAdaptable adaptable = (IAdaptable) object;
                            resource = (IResource) adaptable.getAdapter(IResource.class);
                        } else {
                            LOG.warn("The selected object is not adaptable");
                            LOG.debug("   -> selected object = " + object);
                        }

                        if (resource != null) {
                            resource.accept(this);
                        } else {
                            LOG.warn("The selected object cannot adapt to a resource.");
                            LOG.debug("   -> selected object" + object);
                        }
                    }
                }
            }
        } catch (CoreException e) {
            logError("Core Exception when clearing violations reviews", e);
        }
    }

    /**
     * Clear reviews for a file
     *
     * @param file
     */
    private void clearReviews(IFile file) {
        monitorSubTask(file.getName());

        String updatedFileContent = removeReviews(file);
        if (updatedFileContent != null) {
            saveNewContent(file, updatedFileContent);
        }

        monitorWorked();
    }

    /**
     * remove reviews from file content
     *
     * @param file
     * @return
     */
    private String removeReviews(IFile file) {

        StringWriter modified = new StringWriter();
        boolean noChange = true;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents(), file.getCharset()));
                PrintWriter out = new PrintWriter(modified)) {
            boolean comment = false;

            while (reader.ready()) {
                String origLine = reader.readLine();
                String line = origLine.trim();
                if (line == null) {
                    continue;
                }
                int index = origLine.indexOf(PMDRuntimeConstants.PMD_STYLE_REVIEW_COMMENT);
                int quoteIndex = origLine.indexOf('"');

                if (line.startsWith("/*")) {
                    if (line.indexOf("*/") == -1) {
                        comment = true;
                    }
                    out.println(origLine);
                } else if (comment && line.indexOf("*/") != -1) {
                    comment = false;
                    out.println(origLine);
                } else if (!comment && line.startsWith(PMDRuntimeConstants.PLUGIN_STYLE_REVIEW_COMMENT)) {
                    noChange = false;
                } else if (!comment && index != -1
                        && !(quoteIndex != -1 && quoteIndex < index && index < origLine.lastIndexOf('"'))) {
                    noChange = false;
                    out.println(origLine.substring(0, index));
                } else {
                    out.println(origLine);
                }
            }

            out.flush();

        } catch (CoreException e) {
            logError(StringKeys.ERROR_CORE_EXCEPTION, e);
        } catch (IOException e) {
            logError(StringKeys.ERROR_IO_EXCEPTION, e);
        }

        return noChange ? null : modified.toString();
    }

    /**
     * Save the file
     *
     * @param file
     * @param newContent
     */
    private void saveNewContent(IFile file, String newContent) {
        try {
            file.setContents(new ByteArrayInputStream(newContent.getBytes(file.getCharset())), false, true, getMonitor());
        } catch (CoreException e) {
            logError(StringKeys.ERROR_CORE_EXCEPTION, e);
        } catch (IOException e) {
            logError(StringKeys.ERROR_IO_EXCEPTION, e);
        }
    }

    @Override
    public boolean visit(IResource resource) throws CoreException {
        if (resource instanceof IFile) {
            clearReviews((IFile) resource);
        }

        return resource instanceof IProject || resource instanceof IFolder;
    }

}
