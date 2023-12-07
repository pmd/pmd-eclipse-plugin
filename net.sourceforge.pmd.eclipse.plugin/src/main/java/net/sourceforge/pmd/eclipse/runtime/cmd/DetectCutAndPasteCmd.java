/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPropertyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.CPDReport;
import net.sourceforge.pmd.cpd.CPDReportRenderer;
import net.sourceforge.pmd.cpd.CpdAnalysis;
import net.sourceforge.pmd.cpd.CpdCapableLanguage;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.cmd.internal.CpdResult;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.PropertiesException;
import net.sourceforge.pmd.lang.LanguageRegistry;

/**
 * This command produces a report of the Cut And Paste detector.
 *
 * @author Philippe Herlin, Sven Jacob
 *
 */
public class DetectCutAndPasteCmd extends AbstractProjectCommand {

    private CpdCapableLanguage language;
    private int minTileSize;
    private CPDReportRenderer renderer;
    private String reportName;
    private boolean createReport;
    private List<IPropertyListener> listeners;

    private static final Logger LOG = LoggerFactory.getLogger(DetectCutAndPasteCmd.class);

    /**
     * Default Constructor
     */
    public DetectCutAndPasteCmd() {
        super("DetectCutAndPaste", "Detect Cut & paste for a project");

        setOutputProperties(true);
        setReadOnly(false);
        setTerminated(false);
        listeners = new ArrayList<>();
    }

    private void notifyListeners(final CpdResult cpdResult) {
        // trigger event propertyChanged for all listeners
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                for (IPropertyListener listener : listeners) {
                    listener.propertyChanged(cpdResult, PMDRuntimeConstants.PROPERTY_CPD);
                }
            }
        });
    }

    @Override
    public void execute() {
        try {
            List<File> files = findCandidateFiles();

            if (files.isEmpty()) {
                logInfo("No files found for specified language.");
            } else {
                logInfo("Found " + files.size() + " files for the specified language. Performing CPD.");
            }
            setStepCount(files.size());
            beginTask("Finding suspect Cut And Paste", getStepCount() * 2);

            Consumer<CPDReport> renderer = null;
            if (createReport) {
                renderer = this::renderReport;
            }
            
            if (!isCanceled()) {
                final CpdResult cpdResult = detectCutAndPaste(files, renderer);

                if (!isCanceled()) {
                    notifyListeners(cpdResult);
                }
            }
        } catch (CoreException e) {
            LOG.debug("Core Exception: " + e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (PropertiesException e) {
            LOG.debug("Properties Exception: " + e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            setTerminated(true);
        }
    }

    @Override
    public void reset() {
        super.reset();

        setTerminated(false);
        setReportName(null);
        setCPDRenderer(null);
        setLanguage("java");
        setMinTileSize(PMDPlugin.getDefault().loadPreferences().getMinTileSize());
        setCreateReport(false);
        addPropertyListener(null);
        listeners = new ArrayList<>();
    }

    /**
     * @param LANGUAGE
     *            The language to set.
     */
    public void setLanguage(String theLanguage) {
        language = (CpdCapableLanguage) LanguageRegistry.CPD.getLanguageById(theLanguage);
    }

    /**
     * @param tilesize
     *            The tilesize to set.
     */
    public void setMinTileSize(final int tilesize) {
        minTileSize = tilesize;
    }

    public void setCPDRenderer(CPDReportRenderer theRenderer) {
        this.renderer = theRenderer;
    }

    public void setReportName(final String theReportName) {
        this.reportName = theReportName;
    }

    /**
     * @param render
     *            render a report or not.
     */
    public void setCreateReport(final boolean render) {
        createReport = render;
    }

    /**
     * Adds an object that wants to get an event after the command is finished.
     * 
     * @param listener
     *            the property listener to set.
     */
    public void addPropertyListener(IPropertyListener listener) {
        listeners.add(listener);
    }

    private boolean canRenderReport() {
        return renderer != null && StringUtils.isNotBlank(reportName);
    }

    @Override
    public boolean isReadyToExecute() {
        // need a renderer and reportName if a report should be created
        return super.isReadyToExecute() && language != null && (!createReport || canRenderReport());
    }

    /**
     * Finds all files in a project based on a language. Uses internally the
     * CPDVisitor.
     * 
     * @return List of files
     * @throws PropertiesException
     * @throws CoreException
     */
    private List<File> findCandidateFiles() throws PropertiesException, CoreException {

        final IProjectProperties properties = projectProperties();
        final CPDVisitor visitor = new CPDVisitor();
        visitor.setWorkingSet(properties.getProjectWorkingSet());
        visitor.setIncludeDerivedFiles(properties.isIncludeDerivedFiles());
        visitor.setLanguage(language);
        visitor.setFiles(new ArrayList<>());
        visitProjectResourcesWith(visitor);
        return visitor.getFiles();
    }

    /**
     * Run the cut and paste detector. At first all files have to be added to
     * the cpd. Then the CPD can be executed.
     * 
     * @param files
     *            List of files to be checked.
     * @return the CPD itself for retrieving the matches.
     * @throws CoreException
     */
    private CpdResult detectCutAndPaste(final List<File> files, final Consumer<CPDReport> renderer) {
        LOG.debug("Searching for project files");

        final AtomicReference<CpdResult> reportResult = new AtomicReference<>();

        CPDConfiguration config = new CPDConfiguration();
        config.setMinimumTileSize(minTileSize);
        config.setOnlyRecognizeLanguage(language);
        config.setSourceEncoding(Charset.defaultCharset());

        try (CpdAnalysis cpd = CpdAnalysis.create(config)) {
            subTask("Collecting files for CPD");
            final Iterator<File> fileIterator = files.iterator();
            while (fileIterator.hasNext() && !isCanceled()) {
                final File file = fileIterator.next();
                cpd.files().addFile(file.toPath());
                worked(1);
            }

            if (!isCanceled()) {
                subTask("Performing CPD");
                LOG.debug("Performing CPD");
                cpd.performAnalysis(r -> {
                    if (renderer != null) {
                        renderer.accept(r);
                    }
                    reportResult.set(new CpdResult(r));
                });
                worked(getStepCount());
            }
        } catch (IOException e) {
            LOG.error("IOException while executing CPD", e);
        }

        return reportResult.get();
    }

    /**
     * Renders a report using the matches of the CPD. Creates a report folder
     * and report file.
     * 
     * @param matches
     *            matches of the CPD
     */
    private void renderReport(CPDReport cpdResult) {
        try {
            LOG.debug("Rendering CPD report");
            subTask("Rendering CPD report");

            // Create the report folder if not already existing
            LOG.debug("Create the report folder");
            final IFolder folder = getProjectFolder(PMDRuntimeConstants.REPORT_FOLDER);
            if (!folder.exists()) {
                folder.create(true, true, getMonitor());
            }
            // Create the report file
            LOG.debug("Create the report file");
            final IFile reportFile = folder.getFile(reportName);

            byte[] data = new byte[0];
            try (ByteArrayOutputStream renderedReport = new ByteArrayOutputStream();
                 Writer writer = new OutputStreamWriter(renderedReport);) {
                renderer.render(cpdResult, writer);
                writer.flush();
                data = renderedReport.toByteArray();
            } catch (IOException e) {
                LOG.error("Error while renderering CPD Report", e);
                throw new RuntimeException(e);
            }

            try (InputStream contentsStream = new ByteArrayInputStream(data)) {
                if (reportFile.exists()) {
                    LOG.debug("   Overwriting report file");
                    reportFile.setContents(contentsStream, true, false, getMonitor());
                } else {
                    LOG.debug("   Creating report file");
                    reportFile.create(contentsStream, true, getMonitor());
                }
            } catch (IOException e) {
                LOG.error("Error while writing CPD Report", e);
                throw new RuntimeException(e);
            }

            reportFile.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());

        } catch (CoreException e) {
            LOG.debug("Core Exception: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
