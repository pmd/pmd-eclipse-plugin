/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.ResourceWorkingSetFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Report.ConfigurationError;
import net.sourceforge.pmd.Report.ProcessingError;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.PropertiesException;
import net.sourceforge.pmd.eclipse.ui.actions.internal.InternalRuleSetUtil;
import net.sourceforge.pmd.eclipse.util.internal.IOUtil;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.LanguageVersionDiscoverer;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;

/**
 * Factor some useful features for visitors
 *
 * @author Philippe Herlin
 *
 */
public class BaseVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(BaseVisitor.class);
    private IProgressMonitor monitor;
    @Deprecated
    private boolean useTaskMarker = false;
    private Map<IFile, Set<MarkerInfo2>> accumulator;
    // private PMDEngine pmdEngine;
    private List<RuleSet> ruleSets;
    private Set<String> fileExtensions;
    private int fileCount;
    private long pmdDuration;
    private IProjectProperties projectProperties;

    private PMDConfiguration configuration;

    /**
     * The constructor is protected to avoid illegal instantiation.
     *
     */
    protected BaseVisitor() {
        super();
    }

    protected PMDConfiguration configuration() {
        if (configuration == null) {
            configuration = new PMDConfiguration();
        }
        return configuration;
    }

    /**
     * Returns the useTaskMarker.
     *
     * @return boolean
     * @deprecated not used
     */
    @Deprecated
    public boolean isUseTaskMarker() {
        return useTaskMarker;
    }

    /**
     * Sets the useTaskMarker.
     *
     * @param shouldUseTaskMarker
     *            The useTaskMarker to set
     * @deprecated not used
     */
    @Deprecated
    public void setUseTaskMarker(final boolean shouldUseTaskMarker) {
        this.useTaskMarker = shouldUseTaskMarker;
    }

    /**
     * Returns the accumulator.
     *
     * @return Map
     */
    public Map<IFile, Set<MarkerInfo2>> getAccumulator() {
        return accumulator;
    }

    /**
     * Sets the accumulator.
     *
     * @param accumulator
     *            The accumulator to set
     */
    public void setAccumulator(final Map<IFile, Set<MarkerInfo2>> accumulator) {
        this.accumulator = accumulator;
    }

    public IProgressMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(final IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Tell whether the user has required to cancel the operation.
     */
    public boolean isCanceled() {
        return getMonitor() != null && getMonitor().isCanceled();
    }

    /**
     * Begin a subtask.
     *
     * @param name
     *            the task name
     */
    public void subTask(final String name) {
        if (getMonitor() != null) {
            getMonitor().subTask(name);
        }
    }

    /**
     * Inform of the work progress.
     */
    public void worked(final int work) {
        if (getMonitor() != null) {
            getMonitor().worked(work);
        }
    }

    /**
     * @return Returns all the ruleSets.
     * @deprecated Use {@link #getRuleSetList()}
     */
    @Deprecated
    public net.sourceforge.pmd.RuleSets getRuleSets() {
        return InternalRuleSetUtil.toRuleSets(this.ruleSets);
    }

    public List<RuleSet> getRuleSetList() {
        return this.ruleSets;
    }

    /**
     * This returns the first ruleset file.
     */
    public RuleSet getRuleSet() {
        return this.ruleSets.get(0);
    }
    
    /**
     * This removes all the Rulesets from the rulesets
     * and sets the only ruleset to the one passed in.
     */
    public void setRuleSet(RuleSet ruleSet) {
        this.ruleSets = new ArrayList<>();
        this.ruleSets.add(ruleSet);
    }
    
    /**
     * @param ruleSet
     *            The ruleSet to set.
     * @deprecated Use {@link #setRuleSetList(List)}
     */
    @Deprecated
    public void setRuleSets(final net.sourceforge.pmd.RuleSets ruleSets) {
        setRuleSetList(Arrays.asList(ruleSets.getAllRuleSets()));
    }

    public void setRuleSetList(List<RuleSet> ruleSets) {
        this.ruleSets = ruleSets;
    }

    public void setFileExtensions(Set<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    /**
     * @return the number of files that has been processed
     */
    public int getProcessedFilesCount() {
        return fileCount;
    }

    /**
     * @return actual PMD duration
     */
    public long getActualPmdDuration() {
        return pmdDuration;
    }

    /**
     * Set the project properties (note that visitor is expected to be called one project at a time
     */
    public void setProjectProperties(IProjectProperties projectProperties) {
        this.projectProperties = projectProperties;
    }

    private boolean isIncluded(IFile file) throws PropertiesException {
        return projectProperties.isIncludeDerivedFiles()
                || !projectProperties.isIncludeDerivedFiles() && !file.isDerived();
    }

    /**
     * Run PMD against a resource
     *
     * @param resource
     *            the resource to process
     */
    protected final void reviewResource(IResource resource) {
        if (isCanceled()) {
            return;
        }

        IFile file = (IFile) resource.getAdapter(IFile.class);
        if (file == null || file.getFileExtension() == null) {
            return;
        }

        if (PMDPlugin.getDefault().loadPreferences().isDetermineFiletypesAutomatically()) {
            if (fileExtensions != null) {
                if (!fileExtensions.contains(file.getFileExtension().toLowerCase(Locale.ROOT))) {
                    LOG.debug("Skipping file {} based on file extension", file);
                    return;
                }
            } else {
                LOG.warn("Can't check for file extensions.");
            }
        }

        try {
            boolean included = isIncluded(file);
            LOG.debug("Derived files included: " + projectProperties.isIncludeDerivedFiles());
            LOG.debug("file " + file.getName() + " is derived: " + file.isDerived());
            LOG.debug("file checked: " + included);

            prepareMarkerAccumulator(file);

            LanguageVersionDiscoverer languageDiscoverer = new LanguageVersionDiscoverer();
            LanguageVersion languageVersion = languageDiscoverer.getDefaultLanguageVersionForFile(file.getName());
            // in case it is java, select the correct java version
            if (languageVersion != null
                    && LanguageRegistry.getLanguage(JavaLanguageModule.NAME).equals(languageVersion.getLanguage())) {
                languageVersion = PMDPlugin.javaVersionFor(file.getProject());
            }
            if (languageVersion != null) {
                configuration().setDefaultLanguageVersion(languageVersion);
            }
            LOG.debug("discovered language: {}", languageVersion);

            if (PMDPlugin.getDefault().loadPreferences().isProjectBuildPathEnabled()) {
                configuration().setClassLoader(projectProperties.getAuxClasspath());
            }

            // Avoid warnings about not providing cache for incremental analysis
            configuration().setIgnoreIncrementalAnalysis(true);

            final File sourceCodeFile = file.getRawLocation().toFile();
            if (included && InternalRuleSetUtil.ruleSetsApplies(ruleSets, sourceCodeFile) && isFileInWorkingSet(file)
                    && languageVersion != null) {
                subTask("PMD checking: " + file.getProject() + ": " + file.getName());

                long start = System.currentTimeMillis();

                // need to disable multi threading, as the ruleset is
                // not recreated and shared between threads...
                // but as we anyway have only one file to process, it won't hurt
                // here.
                configuration().setThreads(0);

                Report collectingReport = null;

                try (Reader input = new InputStreamReader(file.getContents(), file.getCharset());
                     PmdAnalysis pmdAnalysis = PmdAnalysis.create(configuration());) {

                    String sourceContents = IOUtil.toString(input);
                    pmdAnalysis.files().addSourceFile(sourceContents, sourceCodeFile.getAbsolutePath());

                    pmdAnalysis.addRuleSets(getRuleSetList());

                    LOG.debug("PMD running on file {}", file.getName());
                    collectingReport = pmdAnalysis.performAnalysisAndCollectReport();
                    LOG.debug("PMD run finished.");
                }

                pmdDuration += System.currentTimeMillis() - start;

                LOG.debug("PMD found {} violations for file {}", collectingReport.getViolations().size(), file);

                if (!collectingReport.getConfigurationErrors().isEmpty()) {
                    StringBuilder message = new StringBuilder("There were configuration errors!\n");
                    for (ConfigurationError error : collectingReport.getConfigurationErrors()) {
                        message.append(error.rule().getName()).append(": ").append(error.issue()).append('\n');
                    }
                    LOG.warn(message.toString());
                }
                if (!collectingReport.getProcessingErrors().isEmpty()) {
                    StringBuilder message = new StringBuilder("There were processing errors!\n");
                    for (ProcessingError error : collectingReport.getProcessingErrors()) {
                        message.append(error.getFile()).append(": ").append(error.getMsg()).append(' ')
                        .append(error.getDetail())
                        .append("\n");
                    }
                    PMDPlugin.getDefault().logWarn(message.toString());
                    throw new RuntimeException(message.toString());
                }

                updateMarkers(file, collectingReport.getViolations());

                worked(1);
                fileCount++;
            } else {
                LOG.debug("The file " + file.getName() + " is not in the working set");
            }

        } catch (CoreException e) {
            // TODO: complete message
            LOG.error("Core exception visiting " + file.getName(), e);
        } catch (IOException e) {
            // TODO: complete message
            LOG.error("IO exception visiting " + file.getName(), e);
        } catch (PropertiesException e) {
            // TODO: complete message
            LOG.error("Properties exception visiting {}", file.getName(), e);
        } catch (IllegalArgumentException e) {
            LOG.error("Illegal argument: {}", e.toString(), e);
        } catch (RuntimeException e) {
            LOG.error("Runtime exception visiting {}", file.getName(), e);
        }

    }

    /**
     * Test if a file is in the PMD working set.
     *
     * @param file
     * @return true if the file should be checked
     */
    private boolean isFileInWorkingSet(final IFile file) throws PropertiesException {
        boolean fileInWorkingSet = true;
        IWorkingSet workingSet = projectProperties.getProjectWorkingSet();
        if (workingSet != null) {
            ResourceWorkingSetFilter filter = new ResourceWorkingSetFilter();
            filter.setWorkingSet(workingSet);
            fileInWorkingSet = filter.select(null, null, file);
        }

        return fileInWorkingSet;
    }

    public static String markerTypeFor(RuleViolation violation) {
        switch (violation.getRule().getPriority()) {
        case HIGH:
            return PMDRuntimeConstants.PMD_MARKER_1;
        case MEDIUM_HIGH:
            return PMDRuntimeConstants.PMD_MARKER_2;
        case MEDIUM:
            return PMDRuntimeConstants.PMD_MARKER_3;
        case MEDIUM_LOW:
            return PMDRuntimeConstants.PMD_MARKER_4;
        case LOW:
            return PMDRuntimeConstants.PMD_MARKER_5;
        default:
            return PMDRuntimeConstants.PMD_MARKER;
        }
    }

    private void prepareMarkerAccumulator(IFile file) {
        Map<IFile, Set<MarkerInfo2>> accumulator = getAccumulator();
        if (accumulator != null) {
            accumulator.put(file, new HashSet<MarkerInfo2>());
        }
    }

    private void updateMarkers(IFile file, List<RuleViolation> violations)
            throws CoreException, PropertiesException {

        Map<IFile, Set<MarkerInfo2>> accumulator = getAccumulator();
        Set<MarkerInfo2> markerSet = new HashSet<>();
        List<Review> reviewsList = findReviewedViolations(file);
        Review review = new Review();
        // final IPreferences preferences =
        // PMDPlugin.getDefault().loadPreferences();

        Rule rule;
        for (RuleViolation violation : violations) {
            rule = violation.getRule();
            review.ruleName = rule.getName();
            review.lineNumber = violation.getBeginLine();

            if (reviewsList.contains(review)) {
                LOG.debug("Ignoring violation of rule " + rule.getName() + " at line " + violation.getBeginLine()
                        + " because of a review.");
                continue;
            }

            // Ryan Gustafson 02/16/2008 - Always use PMD_MARKER, as people
            // get confused as to why PMD problems don't always show up on
            // Problems view like they do when you do build.
            // markerSet.add(getMarkerInfo(violation, fTask ?
            // PMDRuntimeConstants.PMD_TASKMARKER :
            // PMDRuntimeConstants.PMD_MARKER));
            markerSet.add(getMarkerInfo(violation, markerTypeFor(violation)));
            /*
             * if (isDfaEnabled && violation.getRule().usesDFA()) { markerSet.add(getMarkerInfo(violation,
             * PMDRuntimeConstants.PMD_DFA_MARKER)); } else { markerSet.add(getMarkerInfo(violation, fTask ?
             * PMDRuntimeConstants.PMD_TASKMARKER : PMDRuntimeConstants.PMD_MARKER)); }
             */

            LOG.debug("Adding a violation for rule " + rule.getName() + " at line " + violation.getBeginLine());
        }

        if (accumulator != null) {
            LOG.debug("Adding markerSet to accumulator for file " + file.getName());
            accumulator.put(file, markerSet);
        }
    }

    /**
     * Search for reviewed violations in that file
     *
     * @param file
     */
    private List<Review> findReviewedViolations(final IFile file) {
        final List<Review> reviews = new ArrayList<>();
        int lineNumber = 0;
        boolean findLine = false;
        boolean comment = false;
        final Stack<String> pendingReviews = new Stack<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()))) {
            while (reader.ready()) {
                String line = reader.readLine();
                if (line != null) {
                    line = line.trim();
                    lineNumber++;
                    if (line.startsWith("/*")) {
                        comment = line.indexOf("*/") == -1;
                    } else if (comment && line.indexOf("*/") != -1) {
                        comment = false;
                    } else if (!comment && line.startsWith(PMDRuntimeConstants.PLUGIN_STYLE_REVIEW_COMMENT)) {
                        final String tail = line.substring(PMDRuntimeConstants.PLUGIN_STYLE_REVIEW_COMMENT.length());
                        final String ruleName = tail.substring(0, tail.indexOf(':'));
                        pendingReviews.push(ruleName);
                        findLine = true;
                    } else if (!comment && findLine && StringUtils.isNotBlank(line) && !line.startsWith("//")) {
                        findLine = false;
                        while (!pendingReviews.empty()) {
                            // @PMD:REVIEWED:AvoidInstantiatingObjectsInLoops:
                            // by Herlin on 01/05/05 18:36
                            final Review review = new Review();
                            review.ruleName = pendingReviews.pop();
                            review.lineNumber = lineNumber;
                            reviews.add(review);
                        }
                    }
                }
            }

            // if (log.isDebugEnabled()) {
            // for (int i = 0; i < reviewsList.size(); i++) {
            // final Review review = (Review) reviewsList.get(i);
            // log.debug("Review : rule " + review.ruleName + ", line " +
            // review.lineNumber);
            // }
            // }

        } catch (CoreException e) {
            PMDPlugin.getDefault().logError("Core Exception when searching reviewed violations", e);
        } catch (IOException e) {
            PMDPlugin.getDefault().logError("IO Exception when searching reviewed violations", e);
        }

        return reviews;
    }

    private MarkerInfo2 getMarkerInfo(RuleViolation violation, String type) throws PropertiesException {

        Rule rule = violation.getRule();

        MarkerInfo2 info = new MarkerInfo2(type, 7);

        info.add(IMarker.MESSAGE, rule.getName() + ": " + violation.getDescription());
        info.add(PMDRuntimeConstants.KEY_MARKERATT_MESSAGE, violation.getDescription());
        info.add(IMarker.LINE_NUMBER, violation.getBeginLine());
        info.add(PMDRuntimeConstants.KEY_MARKERATT_LINE2, violation.getEndLine());
        info.add(PMDRuntimeConstants.KEY_MARKERATT_RULENAME, rule.getName());
        info.add(PMDRuntimeConstants.KEY_MARKERATT_PRIORITY, rule.getPriority().getPriority());
        info.add(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);

        switch (rule.getPriority()) {
        case HIGH:
        case MEDIUM_HIGH:
            info.add(IMarker.SEVERITY,
                    projectProperties.violationsAsErrors() ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING);
            break;

        case MEDIUM:
        case MEDIUM_LOW:
            info.add(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
            break;

        case LOW:
        default:
            info.add(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
            break;
        }

        return info;
    }

    /**
     * Private inner type to handle reviews.
     */
    private class Review {
        public String ruleName;
        public int lineNumber;

        @Override
        public boolean equals(final Object obj) {
            boolean result = false;
            if (obj instanceof Review) {
                Review reviewObj = (Review) obj;
                result = ruleName.equals(reviewObj.ruleName) && lineNumber == reviewObj.lineNumber;
            }
            return result;
        }

        @Override
        public int hashCode() {
            return ruleName.hashCode() + lineNumber * lineNumber;
        }

    }
}
