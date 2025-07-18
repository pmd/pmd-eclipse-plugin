/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.actions;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.cmd.RenderReportsCmd;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.reports.ReportManager;
import net.sourceforge.pmd.renderers.Renderer;

/**
 * Process GenerateReport action menu. Generate a HTML report on the current
 * project.
 *
 * @author Philippe Herlin
 * @author Brian Remedios
 */
public class GenerateReportAction extends AbstractUIAction {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateReportAction.class);

    private static final String DEFAULT_REPORT_NAME = "pmd-report";

    private void registerRenderers(RenderReportsCmd cmd) {

        ReportManager.loadReportProperties();

        for (Renderer renderer : ReportManager.INSTANCE.activeRenderers()) {
            cmd.registerRenderer(renderer, DEFAULT_REPORT_NAME + "." + renderer.defaultFileExtension());
        }
    }

    private boolean checkRenderers() {

        List<Renderer> renderers = ReportManager.INSTANCE.activeRenderers();

        if (renderers.isEmpty()) {
            PMDPlugin.getDefault().showUserError("No report renderers selected");
            return false;
        }

        StringBuilder errors = new StringBuilder();

        for (Renderer renderer : renderers) {
            String issue = renderer.dysfunctionReason();
            if (StringUtils.isNotBlank(issue)) {
                errors.append(renderer.getName()).append(": ");
                errors.append(issue).append("\n");
            }
        }

        if (errors.length() == 0) {
            return true;
        }

        PMDPlugin.getDefault().showUserError(errors.toString());
        return false;
    }

    @Override
    public final void run(final IAction action) {
        LOG.info("Generation Report action requested");
        final ISelection sel = targetSelection();
        if (sel instanceof IStructuredSelection) {
            try {
                IProject project = getProject((IStructuredSelection) sel);
                if (project != null) {

                    if (!checkRenderers()) {
                        return;
                    }

                    RenderReportsCmd cmd = new RenderReportsCmd();
                    cmd.setProject(project);
                    cmd.setUserInitiated(true);
                    registerRenderers(cmd);
                    cmd.performExecute();
                }
            } catch (RuntimeException e) {
                showErrorById(StringKeys.ERROR_PMD_EXCEPTION, e);
            }
        }
    }

    /**
     * Get a project from a selection
     * 
     * @param selection
     * @return
     */
    private static IProject getProject(IStructuredSelection selection) {
        Object object = selection.getFirstElement();
        if (object instanceof IAdaptable) {
            final IResource resource = (IResource) ((IAdaptable) object).getAdapter(IResource.class);
            if (resource != null) {
                return resource.getProject();
            }
        }
        return null;
    }

}
