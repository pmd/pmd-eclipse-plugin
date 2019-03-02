/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.actions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.apache.log4j.Logger;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.cmd.AbstractDefaultCommand;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.util.IOUtil;

/**
 * Implements the clear reviews action
 *
 * @author Philippe Herlin
 *
 */
public class ClearReviewsAction extends AbstractUIAction implements IResourceVisitor, IViewActionDelegate {

    private static final Logger LOG = Logger.getLogger(ClearReviewsAction.class);
    private IProgressMonitor monitor;

    /**
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view) {
        setActivePart(null, view.getSite().getPage().getActivePart());
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        LOG.info("Remove violation reviews requested.");
        ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
        try {
            monitorDialog.run(false, false, new IRunnableWithProgress() {
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
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * Get the monitor
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
            // If action is started from a view, the process all selected
            // resource
            if (isViewPart()) {
                ISelection selection = targetSelection();

                if (selection != null && selection instanceof IStructuredSelection) {
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
            }

            // If action is started from an editor, process the file currently
            // edited
            if (isEditorPart()) {
                IEditorInput editorInput = ((IEditorPart) this.targetPart()).getEditorInput();
                if (editorInput instanceof IFileEditorInput) {
                    ((IFileEditorInput) editorInput).getFile().accept(this);
                } else {
                    LOG.debug("The kind of editor input is not supported. The editor input if of type: "
                            + editorInput.getClass().getName());
                }
            } else {
                // else this is not supported
                LOG.debug("This action is not supported on this kind of part. This part type is: "
                        + targetPartClassName());
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

    private static boolean isReviewable(IFile file) {

        if (AbstractDefaultCommand.isJavaFile(file)) {
            return true;
        }
        return file.getName().toLowerCase().endsWith(".jsp");
    }

    /**
     * remove reviews from file content
     *
     * @param file
     * @return
     */
    private String removeReviews(IFile file) {

        if (!isReviewable(file)) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter out = null;
        boolean noChange = true;
        try {
            boolean comment = false;
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
            out = new PrintWriter(baos);

            while (reader.ready()) {
                String origLine = reader.readLine();
                String line = origLine.trim();
                if (line == null) {
                    break;
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
        } finally {
            IOUtil.closeQuietly(baos);
            IOUtil.closeQuietly(out);
        }

        return noChange ? null : baos.toString();
    }

    /**
     * Save the file
     *
     * @param file
     * @param newContent
     */
    private void saveNewContent(IFile file, String newContent) {
        try {
            file.setContents(new ByteArrayInputStream(newContent.getBytes()), false, true, getMonitor());
        } catch (CoreException e) {
            logError(StringKeys.ERROR_CORE_EXCEPTION, e);
        }
    }

    /**
     * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
     */
    public boolean visit(IResource resource) throws CoreException {
        if (resource instanceof IFile) {
            clearReviews((IFile) resource);
        }

        return resource instanceof IProject || resource instanceof IFolder;
    }

}
