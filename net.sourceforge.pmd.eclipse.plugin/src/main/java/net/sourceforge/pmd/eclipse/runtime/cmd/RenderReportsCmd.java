/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.builder.MarkerUtil;
import net.sourceforge.pmd.eclipse.util.IOUtil;
import net.sourceforge.pmd.renderers.Renderer;

/**
 * This command produce a report for a project using the specified renderer.
 *
 * @author Philippe Herlin
 *
 */
public class RenderReportsCmd extends AbstractProjectCommand {

    private static final Logger LOG = LoggerFactory.getLogger(RenderReportsCmd.class);

    /**
     * Table containing the renderers indexed by the file name.
     */
    private Map<String, Renderer> renderers = new HashMap<>();

    /**
     * Default Constructor
     */
    public RenderReportsCmd() {
        super("RenderReport", "Produce reports for a project");

        setOutputProperties(false);
        setReadOnly(false);
        setTerminated(false);
    }

    /**
     * Register a renderer and its associated file for processing.
     *
     * @param renderer
     *            the renderer
     * @param reportFile
     *            the file name where the report will be saved
     */
    public void registerRenderer(Renderer renderer, String reportFile) {
        if (reportFile != null && renderer != null) {
            renderers.put(reportFile, renderer);
        }
    }

    /**
     * 
     * @param report
     * @param folder
     * @param reportName
     * @param renderer
     * @throws IOException
     * @throws CoreException
     */
    private void render(Report report, IFolder folder, String reportName, Renderer renderer)
            throws IOException, CoreException {

        StringWriter writer = new StringWriter();
        String reportString = null;

        try {
            renderer.setWriter(writer);
            renderer.start();
            renderer.renderFileReport(report);
            renderer.end();

            reportString = writer.toString();
        } finally {
            IOUtil.closeQuietly(writer);
        }

        if (StringUtils.isBlank(reportString)) {
            LOG.debug("Missing content for report: " + reportName);
            return;
        }

        LOG.debug("   Creating the report file");
        IFile reportFile = folder.getFile(reportName);
        InputStream contentsStream = new ByteArrayInputStream(reportString.getBytes());
        if (reportFile.exists()) {
            reportFile.setContents(contentsStream, true, false, getMonitor());
        } else {
            reportFile.create(contentsStream, true, getMonitor());
        }
        reportFile.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
        contentsStream.close();
    }

    @Override
    public void execute() {

        try {
            LOG.debug("Starting RenderReport command");
            LOG.debug("   Create a report object");
            final Report report = createReport(project());

            LOG.debug("   Getting the report folder");
            final IFolder folder = getProjectFolder(PMDRuntimeConstants.REPORT_FOLDER);
            if (!folder.exists()) {
                folder.create(true, true, getMonitor());
            }

            for (Map.Entry<String, Renderer> entry : renderers.entrySet()) {
                String reportName = entry.getKey();
                Renderer renderer = entry.getValue();
                LOG.debug("   Render the report");
                render(report, folder, reportName, renderer);
            }
        } catch (CoreException | IOException e) {
            LOG.debug(e.toString(), e);
            throw new RuntimeException(e);
        } finally {
            LOG.debug("End of RenderReport command");
            setTerminated(true);
        }
    }

    @Override
    public void reset() {
        setProject(null);
        renderers = new HashMap<>();
        setTerminated(false);
    }

    @Override
    public boolean isReadyToExecute() {
        return super.isReadyToExecute() && !renderers.isEmpty();
    }

    private static void classAndPackageFrom(IMarker marker, FakeRuleViolation violation) throws JavaModelException {
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom((IFile) marker.getResource());

        IPackageDeclaration[] packages = unit.getPackageDeclarations();
        violation.setPackageName(packages.length > 0 ? packages[0].getElementName() : "(default)");

        IType[] types = unit.getAllTypes();
        violation.setClassName(types.length > 0 ? types[0].getElementName() : marker.getResource().getName());
    }

    /**
     * Create a Report object from the markers of a project.
     * 
     * @param project
     * @return
     */
    private Report createReport(IProject project) throws CoreException {

        Report report = new Report();

        IMarker[] markers = MarkerUtil.findAllMarkers(project);
        RuleSet ruleSet = PMDPlugin.getDefault().getPreferencesManager().getRuleSet();
        boolean isJavaProject = project.hasNature(JavaCore.NATURE_ID);

        for (IMarker marker : markers) {
            String ruleName = marker.getAttribute(PMDRuntimeConstants.KEY_MARKERATT_RULENAME, "");
            Rule rule = ruleSet.getRuleByName(ruleName);

            FakeRuleViolation ruleViolation = createViolation(marker, rule);

            if (isJavaProject && marker.getResource() instanceof IFile) {
                classAndPackageFrom(marker, ruleViolation);
            }

            report.addRuleViolation(ruleViolation);
        }

        return report;
    }

    private static FakeRuleViolation createViolation(IMarker marker, Rule rule) {

        // @PMD:REVIEWED:AvoidInstantiatingObjectsInLoops: by Herlin on 01/05/05
        // 19:14
        FakeRuleViolation ruleViolation = new FakeRuleViolation(rule);

        // Fill in the rule violation object before adding it to the report
        ruleViolation.setBeginLine(marker.getAttribute(IMarker.LINE_NUMBER, 0));
        ruleViolation.setEndLine(marker.getAttribute(PMDRuntimeConstants.KEY_MARKERATT_LINE2, 0));
        ruleViolation.setVariableName(marker.getAttribute(PMDRuntimeConstants.KEY_MARKERATT_LINE2, ""));
        ruleViolation.setFilename(marker.getResource().getProjectRelativePath().toString());
        ruleViolation.setDescription(marker.getAttribute(IMarker.MESSAGE, rule.getMessage()));
        return ruleViolation;
    }
}
