/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.properties.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IWorkingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.eclipse.core.internal.FileModificationUtil;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.cmd.JavaProjectClassLoader;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectPropertiesManager;
import net.sourceforge.pmd.eclipse.runtime.properties.PropertiesException;
import net.sourceforge.pmd.eclipse.runtime.writer.IRuleSetWriter;
import net.sourceforge.pmd.eclipse.runtime.writer.WriterException;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;
import net.sourceforge.pmd.eclipse.ui.actions.internal.InternalRuleSetUtil;
import net.sourceforge.pmd.eclipse.util.IOUtil;

/**
 * Implementation of a project properties information structure
 *
 * @author Philippe Herlin
 *
 */
public class ProjectPropertiesImpl implements IProjectProperties {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectPropertiesImpl.class);

    private static final String PROJECT_RULESET_FILE = ".ruleset";

    private final IProjectPropertiesManager projectPropertiesManager;
    private final IProject project;
    private boolean needRebuild;
    private boolean pmdEnabled;
    private boolean ruleSetStoredInProject;
    private String ruleSetFile;
    private List<RuleSet> projectRuleSets;
    private long projectRuleFileLastModified = 0;
    private IWorkingSet projectWorkingSet;
    private boolean includeDerivedFiles;
    private boolean violationsAsErrors = true;
    private boolean fullBuildEnabled = true; // default in case didn't come from properties
    private Set<String> buildPathExcludePatterns = new HashSet<String>();
    private Set<String> buildPathIncludePatterns = new HashSet<String>();
    private ClassLoader auxclasspath;

    /**
     * The default constructor takes a project as an argument
     */
    public ProjectPropertiesImpl(final IProject project, IProjectPropertiesManager projectPropertiesManager) {
        super();
        this.project = project;
        this.projectPropertiesManager = projectPropertiesManager;
        this.projectRuleSets = new ArrayList<>();
        this.projectRuleSets.add(PMDPlugin.getDefault().getPreferencesManager().getRuleSet());
        determineBuildPathIncludesExcludes();
    }

    /**
     * Determines the included and excluded paths configured for the build path of this eclipse project.
     */
    private void determineBuildPathIncludesExcludes() {
        IClasspathEntry source = PMDPlugin.buildSourceClassPathEntryFor(project);
        if (source != null) {
            try {
                String basePath = new File(project.getWorkspace().getRoot().getLocation().toOSString()
                        + java.io.File.separator + source.getPath().toOSString()).getCanonicalPath();
                if (!basePath.endsWith(File.separator)) {
                    basePath += File.separator;
                }
                if (source.getExclusionPatterns() != null) {
                    for (IPath path : source.getExclusionPatterns()) {
                        String pathString = path.toOSString();
                        if (!pathString.endsWith(File.separator)) {
                            pathString += File.separator;
                        }
                        buildPathExcludePatterns.add(basePath + pathString + ".*");
                    }
                }
                if (source.getInclusionPatterns() != null) {
                    for (IPath path : source.getInclusionPatterns()) {
                        String pathString = path.toOSString();
                        if (!pathString.endsWith(File.separator)) {
                            pathString += File.separator;
                        }
                        buildPathIncludePatterns.add(basePath + pathString + ".*");
                    }
                }
            } catch (IOException e) {
                LOG.error("Couldn't determine build class path", e);
            }
        }
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#getProject()
     */
    public IProject getProject() {
        return this.project;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#isPmdEnabled()
     */
    public boolean isPmdEnabled() {
        return this.pmdEnabled;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#setPmdEnabled(boolean)
     */
    public void setPmdEnabled(final boolean pmdEnabled) {
        LOG.debug("Enable PMD for project {}: before={} now={}", this.project.getName(), this.pmdEnabled, pmdEnabled);
        if (this.pmdEnabled != pmdEnabled) {
            this.pmdEnabled = pmdEnabled;
            this.setNeedRebuild(this.needRebuild | pmdEnabled);
        }
    }

    /**
     * @deprecated Use {@link #getProjectRuleSetList()}
     */
    @Deprecated
    @Override
    public RuleSets getProjectRuleSets() throws PropertiesException {
        return InternalRuleSetUtil.toRuleSets(projectRuleSets);
    }

    public List<RuleSet> getProjectRuleSetList() throws PropertiesException {
        return projectRuleSets;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#getProjectRuleSet()
     */
    public RuleSet getProjectRuleSet() throws PropertiesException {
        return projectRuleSets.get(0);
    }

    @Override
    public void setProjectRuleSet(RuleSet projectRuleSet) throws PropertiesException {
        setProjectRuleSetList(Collections.singletonList(projectRuleSet));
    }

    /**
     * @deprecated Use {@link #setProjectRuleSetList(List)}
     */
    @Override
    @Deprecated
    public void setProjectRuleSets(final RuleSets newProjectRuleSets) throws PropertiesException {
        setProjectRuleSetList(Arrays.asList(newProjectRuleSets.getAllRuleSets()));
    }

    @Override
    public void setProjectRuleSetList(List<RuleSet> newProjectRuleSets) throws PropertiesException {
        LOG.debug("Set a rule set for project {}", this.project.getName());
        if (newProjectRuleSets == null) {
            // TODO: NLS
            throw new PropertiesException("Setting a project rule set to null");
        }

        this.setNeedRebuild(!this.projectRuleSets.equals(newProjectRuleSets));
        this.projectRuleSets = newProjectRuleSets;
        if (this.ruleSetStoredInProject) {
            for (File f : getResolvedRuleSetFiles()) {
                long mod = FileModificationUtil.getFileModificationTimestamp(f);
                if (projectRuleFileLastModified < mod) {
                    projectRuleFileLastModified = mod;
                }
            }
        }
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#isRuleSetStoredInProject()
     */
    public boolean isRuleSetStoredInProject() {
        return this.ruleSetStoredInProject;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#setRuleSetStoredInProject(boolean)
     */
    public void setRuleSetStoredInProject(final boolean ruleSetStoredInProject) throws PropertiesException {
        LOG.debug("Set rule set stored in project for project {}: {}", this.project.getName(), ruleSetStoredInProject);
        this.setNeedRebuild(needRebuild | this.ruleSetStoredInProject != ruleSetStoredInProject);
        this.ruleSetStoredInProject = ruleSetStoredInProject;
        if (this.ruleSetStoredInProject) {
            if (!isRuleSetFileExist()) {
                // TODO: NLS
                throw new PropertiesException("The project ruleset file(s) " + getRuleSetFile()
                        + " cannot be found for project " + this.project.getName());
            }
            for (File f : getResolvedRuleSetFiles()) {
                long mod = FileModificationUtil.getFileModificationTimestamp(f);
                if (projectRuleFileLastModified < mod) {
                    projectRuleFileLastModified = mod;
                }
            }
        }
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#getRuleSetFile()
     */
    public String getRuleSetFile() {
        return StringUtils.isBlank(ruleSetFile) ? PROJECT_RULESET_FILE : ruleSetFile;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#setRuleSetFile(String)
     */
    public void setRuleSetFile(String ruleSetFile) throws PropertiesException {
        LOG.debug("Set rule set file for project {}: {}", project.getName(), ruleSetFile);
        this.setNeedRebuild(needRebuild | this.ruleSetFile == null || !this.ruleSetFile.equals(ruleSetFile));
        this.ruleSetFile = ruleSetFile;
        if (ruleSetStoredInProject) {
            if (!isRuleSetFileExist()) {
                // TODO: NLS
                throw new PropertiesException(
                        "The project ruleset file cannot be found for project " + project.getName());
            }
            for (File f : getResolvedRuleSetFiles()) {
                long mod = FileModificationUtil.getFileModificationTimestamp(f);
                if (projectRuleFileLastModified < mod) {
                    projectRuleFileLastModified = mod;
                }
            }
        }
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#getProjectWorkingSet()
     */
    public IWorkingSet getProjectWorkingSet() {
        return this.projectWorkingSet;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#setProjectWorkingSet(org.eclipse.ui.IWorkingSet)
     */
    public void setProjectWorkingSet(final IWorkingSet projectWorkingSet) {
        LOG.debug("Set working set for project {}: {}", project.getName(),
                projectWorkingSet == null ? "none" : projectWorkingSet.getName());

        this.setNeedRebuild(needRebuild | projectWorkingSet == null ? this.projectWorkingSet != null
                : !projectWorkingSet.equals(this.projectWorkingSet));
        this.projectWorkingSet = projectWorkingSet;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#isNeedRebuild()
     */
    @Override
    public boolean isNeedRebuild() {
        LOG.debug("Query if project {} need rebuild : {}", project.getName(), needRebuild);
        if (ruleSetStoredInProject) {
            boolean rulesetFilesChanged = false;
            for (File f : getResolvedRuleSetFiles()) {
                long mod = FileModificationUtil.getFileModificationTimestamp(f);
                rulesetFilesChanged |= mod > projectRuleFileLastModified;
            }
            LOG.debug("   ruleset files have changed = {}", rulesetFilesChanged);
            this.setNeedRebuild(needRebuild | rulesetFilesChanged);
        }
        return needRebuild;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#setNeedRebuild()
     */
    public void setNeedRebuild(final boolean needRebuild) {
        LOG.debug("Set needRebuild for project " + project.getName() + ": " + needRebuild);
        this.needRebuild = needRebuild;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#isRuleSetFileExist()
     */
    public final boolean isRuleSetFileExist() {
        boolean allFilesCanBeRead = false;
        for (File f : getResolvedRuleSetFiles()) {
            if (f.canRead()) {
                allFilesCanBeRead = true;
            } else {
                allFilesCanBeRead = false;
                break;
            }
        }
        return allFilesCanBeRead;
    }

    @Deprecated
    @Override
    public File getResolvedRuleSetFile() throws PropertiesException {
        if (isRuleSetFileExist()) {
            return getResolvedRuleSetFiles().get(0);
        }
        return null;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#getResolvedRuleSetFiles()
     */
    @Override
    public List<File> getResolvedRuleSetFiles() {
        // Check as project-relative path
        List<File> files = new ArrayList<File>();
        for (String ruleSetFile : getRuleSetFile().split(",")) {
            IFile file = project.getFile(ruleSetFile);
            File f = getExistingFileOrNull(file);
            if (f == null) {
                // Check as workspace-relative path
                IWorkspaceRoot workspaceRoot = project.getWorkspace().getRoot();
                try {
                    IFile workspaceFile = workspaceRoot.getFile(new Path(ruleSetFile));
                    f = getExistingFileOrNull(workspaceFile);
                } catch (IllegalArgumentException e) {
                    // Fall back to below
                }
                if (f == null) {
                    // Fall back to file system path
                    f = new File(ruleSetFile);
                    if (!f.canRead()) {
                        f = null;
                    }
                }
            }
            if (f != null) {
                files.add(f);
            }
        }
        return files;
    }

    private File getExistingFileOrNull(IFile file) {
        // try to refresh the resource first - if the file has been created or deleted or modified externally
        // eclipse might not know about it yet
        try {
            file.refreshLocal(IResource.DEPTH_ZERO, null);
        } catch (CoreException e) {
            LOG.warn("Error refreshing {}", file, e);
        }

        boolean exists = file.exists() && file.isAccessible();
        File result = null;
        if (exists) {
            result = file.getLocation().toFile();
        }
        return result;
    }

    /**
     * Create a project ruleset file from the current configured rules
     *
     */
    public void createDefaultRuleSetFile() throws PropertiesException {
        LOG.info("Create a default rule set file for project " + this.project.getName());
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        try {
            IRuleSetWriter writer = PMDPlugin.getDefault().getRuleSetWriter();
            baos = new ByteArrayOutputStream();
            
            // create a single ruleset from all the rulesets
            // the ruleset writer is only capable of writing one ruleset
            RuleSet ruleSet = RuleSetUtil.newEmpty(RuleSetUtil.DEFAULT_RULESET_NAME,
                    RuleSetUtil.DEFAULT_RULESET_DESCRIPTION);
            for (RuleSet rs : projectRuleSets) {
                ruleSet = RuleSetUtil.addRules(ruleSet, rs.getRules());
                ruleSet = InternalRuleSetUtil.addFileInclusions(ruleSet, rs.getFileInclusions());
                ruleSet = InternalRuleSetUtil.addFileExclusions(ruleSet, rs.getFileExclusions());
            }
            writer.write(baos, ruleSet);

            final IFile file = project.getFile(PROJECT_RULESET_FILE);
            if (file.exists() && file.isAccessible()) {
                throw new PropertiesException("Project ruleset file already exists");
            } else {
                bais = new ByteArrayInputStream(baos.toByteArray());
                file.create(bais, true, null);
            }
        } catch (WriterException e) {
            throw new PropertiesException("Error while creating default ruleset file for project " + this.project.getName(), e);
        } catch (CoreException e) {
            throw new PropertiesException("Error while creating default ruleset file for project " + this.project.getName(), e);
        } finally {
            IOUtil.closeQuietly(baos);
            IOUtil.closeQuietly(bais);
        }
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#isIncludeDerivedFiles()
     */
    public boolean isIncludeDerivedFiles() {
        return includeDerivedFiles;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#setIncludeDerivedFiles(boolean)
     */
    public void setIncludeDerivedFiles(boolean includeDerivedFiles) {
        LOG.debug("Set if derived files should be included: " + includeDerivedFiles);
        this.setNeedRebuild(this.needRebuild | this.includeDerivedFiles != includeDerivedFiles);
        this.includeDerivedFiles = includeDerivedFiles;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.model.PMDPluginModel#sync()
     */
    public void sync() throws PropertiesException {
        LOG.info("Commit properties for project " + project.getName());
        projectPropertiesManager.storeProjectProperties(this);
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#violationsAsErrors()
     */
    public boolean violationsAsErrors() throws PropertiesException {
        return this.violationsAsErrors;
    }

    public void setViolationsAsErrors(boolean violationsAsErrors) throws PropertiesException {
        LOG.debug("Set to handle violations as errors: {}", violationsAsErrors);
        this.setNeedRebuild(needRebuild | this.violationsAsErrors != violationsAsErrors);
        this.violationsAsErrors = violationsAsErrors;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#isFullBuildEnabled()
     */
    public boolean isFullBuildEnabled() throws PropertiesException {
        return fullBuildEnabled;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties#setFullBuildEnabled(boolean)
     */
    public void setFullBuildEnabled(boolean fullBuildEnabled) throws PropertiesException {
        LOG.debug("Set if run at full build for project {}: {}", project.getName(), fullBuildEnabled);
        if (this.fullBuildEnabled != fullBuildEnabled) {
            this.fullBuildEnabled = fullBuildEnabled;
            if (this.fullBuildEnabled) {
                this.setNeedRebuild(true);
            }
        }
    }

    /**
     * Provide some help to folks using the debugger and logging
     */
    public String toString() {
        String projectName = "n/a";
        String projectRuleSetName = "n/a";
        String projectWorkingSetName = "n/a";
        if (project != null) {
            projectName = project.getName();
        }
        if (projectRuleSets != null) {
            projectRuleSetName = "";
            for (RuleSet rs : projectRuleSets) {
                projectRuleSetName += rs.getName() + ",";
            }
        }
        if (projectWorkingSet != null) {
            projectWorkingSetName = projectWorkingSet.getName();
        }

        return "fullBuildEnabled:" + fullBuildEnabled + " includeDerivedFiles:" + includeDerivedFiles + " pmdEnabled:"
                + pmdEnabled + " project:" + projectName + " projectRuleSet:" + projectRuleSetName
                + " projectWorkingSet:" + projectWorkingSetName + " ruleSetFile:" + ruleSetFile
                + " ruleSetStoredInProject:" + ruleSetStoredInProject + " violationsAsErrors: " + violationsAsErrors;
    }

    public Set<String> getBuildPathExcludePatterns() {
        return buildPathExcludePatterns;
    }

    public Set<String> getBuildPathIncludePatterns() {
        return buildPathIncludePatterns;
    }

    @Override
    public ClassLoader getAuxClasspath() {
        try {
            if (project != null && project.hasNature(JavaCore.NATURE_ID)) {
                if (auxclasspath == null) {
                    PMDPlugin.getDefault()
                            .logInformation("Creating new auxclasspath class loader for project " + project.getName());
                    auxclasspath = new JavaProjectClassLoader(PMD.class.getClassLoader(), project);
                }
                return auxclasspath;
            }
        } catch (CoreException e) {
            LOG.error("Error determining aux classpath", e);
            PMDPlugin.getDefault().logError("Error determining aux classpath", e);
        }
        return null;
    }
}
