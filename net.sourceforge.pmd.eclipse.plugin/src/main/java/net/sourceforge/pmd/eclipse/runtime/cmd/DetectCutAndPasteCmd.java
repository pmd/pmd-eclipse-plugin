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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPropertyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.cpd.CPD;
import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.Language;
import net.sourceforge.pmd.cpd.LanguageFactory;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.Renderer;
import net.sourceforge.pmd.cpd.renderer.CPDRenderer;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.PropertiesException;

/**
 * This command produces a report of the Cut And Paste detector.
 *
 * @author Philippe Herlin, Sven Jacob
 *
 */
public class DetectCutAndPasteCmd extends AbstractProjectCommand {

    private Language language;
    private int minTileSize;
    private CPDRenderer renderer;
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
        listeners = new ArrayList<IPropertyListener>();
    }

    private void notifyListeners(final CPD cpd) {
        // trigger event propertyChanged for all listeners
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                for (IPropertyListener listener : listeners) {
                    listener.propertyChanged(cpd.getMatches(), PMDRuntimeConstants.PROPERTY_CPD);
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

            if (!isCanceled()) {
                final CPD cpd = detectCutAndPaste(files);

                if (!isCanceled()) {
                    if (createReport) {
                        renderReport(cpd.getMatches());
                    }
                    notifyListeners(cpd);
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
        listeners = new ArrayList<IPropertyListener>();
    }

    /**
     * @param LANGUAGE
     *            The language to set.
     */
    public void setLanguage(String theLanguage) {
        language = LanguageFactory.createLanguage(theLanguage);
    }

    /**
     * @param tilesize
     *            The tilesize to set.
     */
    public void setMinTileSize(final int tilesize) {
        minTileSize = tilesize;
    }

    /**
     * @deprecated Use {@link #setCPDRenderer(CPDRenderer)} instead.
     */
    @Deprecated
    public void setRenderer(final Renderer renderer) {
        if (renderer != null) {
            this.setCPDRenderer(new CPDRenderer() {
                @Override
                public void render(Iterator<Match> matches, Writer writer) throws IOException {
                    writer.write(renderer.render(matches));
                }
            });
        } else {
            this.setCPDRenderer(null);
        }
    }

    public void setCPDRenderer(CPDRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * @param reportName
     *            The reportName to set.
     */
    public void setReportName(final String reportName) {
        this.reportName = reportName;
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
        visitor.setFiles(new ArrayList<File>());
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
    private CPD detectCutAndPaste(final List<File> files) {
        LOG.debug("Searching for project files");

        final CPD cpd = newCPD();

        subTask("Collecting files for CPD");
        final Iterator<File> fileIterator = files.iterator();
        while (fileIterator.hasNext() && !isCanceled()) {
            final File file = fileIterator.next();
            try {
                cpd.add(file);
                worked(1);
            } catch (IOException e) {
                LOG.warn("IOException when adding file " + file.getName() + " to CPD. Continuing.", e);
            }
        }

        if (!isCanceled()) {
            subTask("Performing CPD");
            LOG.debug("Performing CPD");
            cpd.go();
            worked(getStepCount());
        }

        return cpd;
    }

    private CPD newCPD() {
        CPDConfiguration config = new CPDConfiguration();
        config.setMinimumTileSize(minTileSize);
        config.setLanguage(language);
        config.setEncoding(System.getProperty("file.encoding"));
        return new CPD(config);
    }

    /**
     * Renders a report using the matches of the CPD. Creates a report folder
     * and report file.
     * 
     * @param matches
     *            matches of the CPD
     */
    private void renderReport(Iterator<Match> matches) {
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
                renderer.render(matches, writer);
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
