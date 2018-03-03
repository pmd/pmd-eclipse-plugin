/*
 * Created on 12 avr. 2005
 *
 * Copyright (c) 2005, PMD for Eclipse Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * The end-user documentation included with the redistribution, if
 *       any, must include the following acknowledgement:
 *       "This product includes software developed in part by support from
 *        the Defense Advanced Research Project Agency (DARPA)"
 *     * Neither the name of "PMD for Eclipse Development Team" nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.builder.MarkerUtil;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.PropertiesException;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;
import net.sourceforge.pmd.util.StringUtil;

import name.herlin.command.CommandException;
import name.herlin.command.Timer;

/**
 * This command executes the PMD engine on a specified resource
 *
 * @author Philippe Herlin
 *
 */
public class ReviewCodeCmd extends AbstractDefaultCommand {

    private static final Logger LOG = Logger.getLogger(ReviewCodeCmd.class);

    private final List<ISchedulingRule> resources = new ArrayList<ISchedulingRule>();
    private IResourceDelta resourceDelta;
    private Map<IFile, Set<MarkerInfo2>> markersByFile = new HashMap<IFile, Set<MarkerInfo2>>();
    private boolean taskMarker;
    private boolean openPmdPerspective;
    private int ruleCount;
    private int fileCount;
    private long pmdDuration;
    private String onErrorIssue = null;
    /**
     * Whether to run the review command, even if PMD is disabled in the project
     * settings.
     */
    private boolean runAlways = false;
    /**
     * Maximum count of changed resources, that are considered to be not a full
     * build. If more than these resources are changed, PMD will only be
     * executed, if full build option is enabled.
     */
    private static final int MAXIMUM_RESOURCE_COUNT = 5;

    private IProjectProperties propertyCache = null;

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public ReviewCodeCmd() {
        super("ReviewCode", "Run PMD on a list of workbench resources");

        setOutputProperties(true);
        setReadOnly(true);
        setTerminated(false);
    }

    public Set<IFile> markedFiles() {
        return markersByFile.keySet();
    }

    private RuleSet currentRules() {
        // FIXME
        return RuleSetUtil.newEmpty(RuleSetUtil.DEFAULT_RULESET_NAME, RuleSetUtil.DEFAULT_RULESET_DESCRIPTION);
    }

    private Map<Rule, String> misconfiguredRulesIn(RuleSet ruleset) {

        RuleSet ruleSet = currentRules();

        Map<Rule, String> faultsByRule = new HashMap<Rule, String>();
        for (Rule rule : ruleSet.getRules()) {
            String fault = rule.dysfunctionReason();
            if (StringUtil.isNotEmpty(fault)) {
                faultsByRule.put(rule, fault);
            }
        }

        return faultsByRule;
    }

    private boolean checkForMisconfiguredRules() {

        RuleSet ruleSet = currentRules();
        if (ruleSet.getRules().isEmpty())
            return true;

        Map<Rule, String> faultsByRule = misconfiguredRulesIn(ruleSet);
        if (faultsByRule.isEmpty())
            return true;

        return MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Rule configuration problem",
                "Continue anyways?");
    }

    /**
     * @see name.herlin.command.AbstractProcessableCommand#execute()
     */
    @Override
    public void execute() throws CommandException {

        boolean doReview = checkForMisconfiguredRules();
        if (!doReview)
            return;

        LOG.info("ReviewCode command starting.");
        try {
            fileCount = 0;
            ruleCount = 0;
            pmdDuration = 0;

            beginTask("PMD checking...", getStepCount());

            // Lancer PMD
            // PMDPlugin fills resources if it's a full build and
            // resourcesDelta if it is incremental or auto
            if (resources.isEmpty()) {
                processResourceDelta();
            } else {
                processResources();
            }

            // do we really need to do any of the rest of this if
            // fileCount and ruleCount are both 0?

            // skip the marking processing if the markersByFile set is empty
            // (avoids grabbing the "run" lock for nothing)
            if (!markersByFile.isEmpty()) {
                // Appliquer les marqueurs
                IWorkspaceRunnable action = new IWorkspaceRunnable() {
                    public void run(IProgressMonitor monitor) throws CoreException {
                        applyMarkers();
                    }
                };

                // clear the markers here. The call to Resource.deleteMarkers
                // will
                // also call the Workspace.prepareOperation so do that
                // outside the larger "applyMarkers" call to avoid doubly
                // holding locks
                // for too long
                for (IFile file : markersByFile.keySet()) {
                    if (isCanceled())
                        break;
                    MarkerUtil.deleteAllMarkersIn(file);
                }

                final IWorkspace workspace = ResourcesPlugin.getWorkspace();
                workspace.run(action, getSchedulingRule(), IWorkspace.AVOID_UPDATE, getMonitor());
            }

            // Switch to the PMD perspective if required
            if (openPmdPerspective) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        switchToPmdPerspective();
                    }
                });
            }

        } catch (CoreException e) {
            throw new CommandException("Core exception when reviewing code", e);
        } finally {
            LOG.info("ReviewCode command has ended.");
            setTerminated(true);
            done();

            // Log performance information
            if (fileCount > 0 && ruleCount > 0) {
                logInfo("Review code command terminated. " + ruleCount + " rules were executed against " + fileCount
                        + " files. Actual PMD duration is about " + pmdDuration + "ms, that is about "
                        + (float) pmdDuration / fileCount + " ms/file, " + (float) pmdDuration / ruleCount
                        + " ms/rule, " + (float) pmdDuration / ((long) fileCount * (long) ruleCount) + " ms/filerule");
            } else {
                logInfo("Review code command terminated. " + ruleCount + " rules were executed against " + fileCount
                        + " files. PMD was not executed.");
            }
        }

        PMDPlugin.getDefault().changedFiles(markedFiles());
    }

    /**
     * @return Returns the file markers
     */
    public Map<IFile, Set<MarkerInfo2>> getMarkers() {
        return markersByFile;
    }

    public int getFileCount() {
        return fileCount;
    }

    /**
     * @param resource
     *            The resource to set.
     */
    public void setResources(Collection<ISchedulingRule> resources) {
        resources.clear();
        resources.addAll(resources);
    }

    /**
     * Add a resource to the list of resources to be reviewed.
     *
     * @param resource
     *            a workbench resource
     */
    public void addResource(IResource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource parameter can not be null");
        }

        resources.add(resource);
    }

    /**
     * @param resourceDelta
     *            The resourceDelta to set.
     */
    public void setResourceDelta(IResourceDelta resourceDelta) {
        this.resourceDelta = resourceDelta;
    }

    /**
     * @param taskMarker
     *            The taskMarker to set.
     */
    public void setTaskMarker(boolean taskMarker) {
        this.taskMarker = taskMarker;
    }

    public void setRunAlways(boolean runAlways) {
        this.runAlways = runAlways;
    }

    /**
     * @param openPmdPerspective
     *            Tell whether the PMD perspective should be opened after
     *            processing.
     */
    public void setOpenPmdPerspective(boolean openPmdPerspective) {
        this.openPmdPerspective = openPmdPerspective;
    }

    /**
     * @see name.herlin.command.Command#reset()
     */
    @Override
    public void reset() {
        resources.clear();
        markersByFile = new HashMap<IFile, Set<MarkerInfo2>>();
        setTerminated(false);
        openPmdPerspective = false;
        onErrorIssue = null;
        runAlways = false;
    }

    /**
     * @see name.herlin.command.Command#isReadyToExecute()
     */
    @Override
    public boolean isReadyToExecute() {
        return resources.size() != 0 || resourceDelta != null;
    }

    /**
     * @return the scheduling rule needed to apply markers
     */
    private ISchedulingRule getSchedulingRule() {
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IResourceRuleFactory ruleFactory = workspace.getRuleFactory();
        ISchedulingRule rule;

        if (resources.isEmpty()) {
            rule = ruleFactory.markerRule(resourceDelta.getResource().getProject());
        } else {
            ISchedulingRule[] rules = new ISchedulingRule[resources.size()];
            for (int i = 0; i < rules.length; i++) {
                rules[i] = ruleFactory.markerRule((IResource) resources.get(i));
            }
            rule = new MultiRule(resources.toArray(rules));
        }

        return rule;
    }

    /**
     * Process the list of workbench resources
     *
     * @throws CommandException
     */
    private void processResources() throws CommandException {
        final Iterator<ISchedulingRule> i = resources.iterator();
        while (i.hasNext()) {
            final IResource resource = (IResource) i.next();

            // if resource is a project, visit only its source folders
            if (resource instanceof IProject) {
                processProject((IProject) resource);
            } else {
                processResource(resource);
            }
        }
    }

    private IProjectProperties getProjectProperties(IProject project) throws PropertiesException, CommandException {
        if (propertyCache == null || !propertyCache.getProject().getName().equals(project.getName())) {
            propertyCache = PMDPlugin.getDefault().loadProjectProperties(project);
        }
        return propertyCache;
    }

    private RuleSet rulesetFrom(IResource resource) throws PropertiesException, CommandException {
        IProject project = resource.getProject();
        IProjectProperties properties = getProjectProperties(project);

        return filteredRuleSet(properties); // properties.getProjectRuleSet();
    }

    /**
     * Review a single resource
     */
    private void processResource(IResource resource) throws CommandException {
        try {

            final IProject project = resource.getProject();
            final IProjectProperties properties = getProjectProperties(project);
            if (!runAlways && !properties.isPmdEnabled()) {
                return;
            }

            final RuleSet ruleSet = rulesetFrom(resource); // properties.getProjectRuleSet();

            // final PMDEngine pmdEngine = getPmdEngineForProject(project);
            int targetCount = 0;
            if (resource.exists()) {
                targetCount = countResourceElement(resource);
            }
            // Could add a property that lets us set the max number to analyze
            if (properties.isFullBuildEnabled() || isUserInitiated() || targetCount <= MAXIMUM_RESOURCE_COUNT) {
                setStepCount(targetCount);
                LOG.debug("Visiting resource " + resource.getName() + " : " + getStepCount());

                if (resource.exists()) {
                    final ResourceVisitor visitor = new ResourceVisitor();
                    visitor.setMonitor(getMonitor());
                    visitor.setRuleSet(ruleSet);
                    // visitor.setPmdEngine(pmdEngine);
                    visitor.setAccumulator(markersByFile);
                    visitor.setUseTaskMarker(taskMarker);
                    visitor.setProjectProperties(properties);
                    resource.accept(visitor);

                    ruleCount = ruleSet.getRules().size();
                    fileCount += visitor.getProcessedFilesCount();
                    pmdDuration += visitor.getActualPmdDuration();
                } else {
                    LOG.debug("Skipping resource " + resource.getName() + " because it doesn't exist.");
                }
            } else {
                String message = "Skipping resource " + resource.getName() + " because of fullBuildEnabled flag and "
                        + "targetCount is " + targetCount + ". This is more than " + MAXIMUM_RESOURCE_COUNT + "."
                        + " If you want to execute PMD, please check \"Full build enabled\" in the project settings";
                PMDPlugin.getDefault().logInformation(message);
            }

            worked(1); // TODO - temp fix? BR

        } catch (PropertiesException e) {
            throw new CommandException(e);
        } catch (CoreException e) {
            throw new CommandException(e);
        }
    }

    /**
     * Review an entire project
     */
    private void processProject(IProject project) throws CommandException {
        try {
            setStepCount(countResourceElement(project));
            LOG.debug("Visiting  project " + project.getName() + " : " + getStepCount());

            if (project.hasNature(JavaCore.NATURE_ID)) {
                processJavaProject(project);
            } else {
                processResource(project);
            }

        } catch (CoreException e) {
            throw new CommandException(e);
        }
    }

    private void processJavaProject(IProject project) throws CoreException, CommandException {
        final IJavaProject javaProject = JavaCore.create(project);
        final IClasspathEntry[] entries = javaProject.getRawClasspath();
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        for (IClasspathEntry entrie : entries) {
            if (entrie.getEntryKind() == IClasspathEntry.CPE_SOURCE) {

                // phherlin note: this code is ugly but I don't how to do
                // otherwise.
                // The IWorkspaceRoot getContainerLocation(IPath) always
                // return null.
                // Catching the IllegalArgumentException on getFolder is the
                // only way I found
                // to know if the entry is a folder or a project !
                IContainer sourceContainer = null;
                try {
                    sourceContainer = root.getFolder(entrie.getPath());
                } catch (IllegalArgumentException e) {
                    sourceContainer = root.getProject(entrie.getPath().toString());
                }
                if (sourceContainer == null) {
                    LOG.warn("Source container " + entrie.getPath() + " for project " + project.getName()
                            + " is not valid");
                } else {
                    processResource(sourceContainer);
                }
            }
        }
    }

    private void taskScope(int activeRuleCount, int totalRuleCount) {
        setTaskName("Checking with " + Integer.toString(activeRuleCount) + " out of " + Integer.toString(totalRuleCount)
                + " rules");
    }

    private RuleSet filteredRuleSet(IProjectProperties properties) throws CommandException, PropertiesException {

        final RuleSet ruleSet = properties.getProjectRuleSet();
        IPreferences preferences = PMDPlugin.getDefault().getPreferencesManager().loadPreferences();
        Set<String> onlyActiveRuleNames = preferences.getActiveRuleNames();

        int rulesBefore = ruleSet.size();
        RuleSet filteredRuleSet = RuleSetUtil.newCopyOf(ruleSet);
        if (preferences.getGlobalRuleManagement()) {
            // TODO: active rules are not language aware... filter by rule
            // name...
            List<Rule> rulesToKeep = new ArrayList<Rule>();
            for (Rule rule : filteredRuleSet.getRules()) {
                if (onlyActiveRuleNames.contains(rule.getName())) {
                    rulesToKeep.add(rule);
                }
            }
            filteredRuleSet = RuleSetUtil.retainOnly(filteredRuleSet, rulesToKeep);
            int rulesAfter = filteredRuleSet.size();

            if (rulesAfter < rulesBefore) {
                PMDPlugin.getDefault()
                        .logWarn("Ruleset has been filtered as Global Rule Management is active. " + rulesAfter + " of "
                                + rulesBefore + " rules are active and are used. " + (rulesBefore - rulesAfter)
                                + " rules will be ignored.");
            }
        }
        filteredRuleSet = RuleSetUtil.addExcludePatterns(filteredRuleSet, preferences.activeExclusionPatterns(),
                properties.getBuildPathExcludePatterns());
        filteredRuleSet = RuleSetUtil.addIncludePatterns(filteredRuleSet, preferences.activeInclusionPatterns(),
                properties.getBuildPathIncludePatterns());

        taskScope(filteredRuleSet.getRules().size(), ruleSet.getRules().size());
        return filteredRuleSet;
    }

    private RuleSet rulesetFromResourceDelta() throws PropertiesException, CommandException {

        IResource resource = resourceDelta.getResource();
        final IProject project = resource.getProject();
        final IProjectProperties properties = getProjectProperties(project);

        return filteredRuleSet(properties); // properties.getProjectRuleSet();
    }

    /**
     * Review a resource delta
     */
    private void processResourceDelta() throws CommandException {
        try {
            IResource resource = resourceDelta.getResource();
            final IProject project = resource.getProject();
            final IProjectProperties properties = getProjectProperties(project);

            RuleSet ruleSet = rulesetFromResourceDelta(); // properties.getProjectRuleSet();

            // PMDEngine pmdEngine = getPmdEngineForProject(project);
            int targetCount = countDeltaElement(resourceDelta);
            // Could add a property that lets us set the max number to analyze
            if (properties.isFullBuildEnabled() || isUserInitiated() || targetCount <= MAXIMUM_RESOURCE_COUNT) {
                setStepCount(targetCount);
                LOG.debug("Visiting delta of resource " + resource.getName() + " : " + getStepCount());

                DeltaVisitor visitor = new DeltaVisitor();
                visitor.setMonitor(getMonitor());
                visitor.setRuleSet(ruleSet);
                // visitor.setPmdEngine(pmdEngine);
                visitor.setAccumulator(markersByFile);
                visitor.setUseTaskMarker(taskMarker);
                visitor.setProjectProperties(properties);
                resourceDelta.accept(visitor);

                ruleCount = ruleSet.getRules().size();
                fileCount += visitor.getProcessedFilesCount();
                pmdDuration += visitor.getActualPmdDuration();
            } else {
                String message = "Skipping resourceDelta " + resource.getName()
                        + " because of fullBuildEnabled flag and " + "targetCount is " + targetCount
                        + ". This is more than " + MAXIMUM_RESOURCE_COUNT + "."
                        + " If you want to execute PMD, please check \"Full build enabled\" in the project settings";
                PMDPlugin.getDefault().logInformation(message);
                LOG.debug(message);
            }

        } catch (PropertiesException e) {
            throw new CommandException(e);
        } catch (CoreException e) {
            throw new CommandException(e);
        }
    }

    /**
     * Apply PMD markers after the review
     *
     */
    private void applyMarkers() {
        LOG.info("Processing marker directives");
        int violationCount = 0;
        final Timer timer = new Timer();

        String currentFile = ""; // for logging

        beginTask("PMD Applying markers", markersByFile.size());

        try {
            for (IFile file : markersByFile.keySet()) {
                if (isCanceled())
                    break;
                currentFile = file.getName();
                Set<MarkerInfo2> markerInfoSet = markersByFile.get(file);
                for (MarkerInfo2 markerInfo : markerInfoSet) {
                    markerInfo.addAsMarkerTo(file);
                    violationCount++;
                }

                worked(1);
            }
        } catch (CoreException e) {
            LOG.warn("CoreException when setting marker for file " + currentFile + " : " + e.getMessage()); // TODO:
                                                                                                            // NLS
        } finally {
            timer.stop();
            int count = markersByFile.size();
            logInfo("" + violationCount + " markers applied on " + count + " files in " + timer.getDuration() + "ms.");
            LOG.info("End of processing marker directives. " + violationCount + " violations for " + count + " files.");
        }
    }

    /**
     * Count the number of sub-resources of a resource
     *
     * @param resource
     *            a project
     * @return the element count
     */
    private int countResourceElement(IResource resource) {

        if (resource instanceof IFile) {
            return 1;
        }

        final CountVisitor visitor = new CountVisitor();

        try {
            resource.accept(visitor);
        } catch (CoreException e) {
            logError("Exception when counting elements of a project", e);
        }

        return visitor.count;
    }

    /**
     * Count the number of sub-resources of a delta
     *
     * @param delta
     *            a resource delta
     * @return the element count
     */
    private int countDeltaElement(IResourceDelta delta) {
        final CountVisitor visitor = new CountVisitor();

        try {
            delta.accept(visitor);
        } catch (CoreException e) {
            logError("Exception counting elements in a delta selection", e);
        }

        return visitor.count;
    }

    /**
     * opens the PMD perspective
     *
     * @author SebastianRaffel ( 07.05.2005 )
     */
    private static void switchToPmdPerspective() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IPerspectiveRegistry reg = workbench.getPerspectiveRegistry();
        final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        window.getActivePage().setPerspective(reg.findPerspectiveWithId(PMDRuntimeConstants.ID_PERSPECTIVE));
    }

    /**
     * Private inner class to count the number of resources or delta elements.
     * Only files are counted.
     */
    private final class CountVisitor implements IResourceVisitor, IResourceDeltaVisitor {
        public int count = 0;

        public boolean visit(IResource resource) {
            if (resource instanceof IFile) {
                count++;
            }
            return true;
        }

        public boolean visit(IResourceDelta delta) {
            IResource resource = delta.getResource();
            return visit(resource);
        }
    }

}
