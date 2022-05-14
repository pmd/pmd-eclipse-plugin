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
    private Set<String> buildPathExcludePatterns = new HashSet<>();
    private Set<String> buildPathIncludePatterns = new HashSet<>();
    private JavaProjectClassLoader auxclasspath;

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
            String basePath = project.getWorkspace().getRoot()
                    .getFolder(source.getPath()).getLocation().toPortableString();
            if (!basePath.endsWith(String.valueOf(IPath.SEPARATOR))) {
                basePath += IPath.SEPARATOR;
            }
            if (source.getExclusionPatterns() != null) {
                for (IPath path : source.getExclusionPatterns()) {
                    String pathString = path.toPortableString();
                    buildPathExcludePatterns.add(basePath + convertPatternToRegex(pathString));
                }
            }
            if (source.getInclusionPatterns() != null) {
                for (IPath path : source.getInclusionPatterns()) {
                    String pathString = path.toPortableString();
                    buildPathIncludePatterns.add(basePath + convertPatternToRegex(pathString));
                }
            }
        }
    }

    /**
     * Simple conversion from the Ant-like pattern to regex pattern.
     */
    private String convertPatternToRegex(String pattern) {
        String regex = pattern;
        regex = regex.replaceAll("\\.", "\\\\."); // replace "." with "\\."
        regex = regex.replaceAll("\\*\\*", ".*"); // replace "**" with ".*"
        regex = regex.replaceAll("/\\*([^\\*])", "/[^/]*$1"); // replace "/*" with "/[^/]*"
        regex = regex.replaceAll("\\.\\*/", ".*"); // replace ".*/" with ".*"
        regex = regex.replaceAll("\\?", "."); // replace "?" with "."
        return regex;
    }

    @Override
    public IProject getProject() {
        return this.project;
    }

    @Override
    public boolean isPmdEnabled() {
        return this.pmdEnabled;
    }

    @Override
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
    public net.sourceforge.pmd.RuleSets getProjectRuleSets() throws PropertiesException {
        return InternalRuleSetUtil.toRuleSets(projectRuleSets);
    }

    @Override
    public List<RuleSet> getProjectRuleSetList() throws PropertiesException {
        return projectRuleSets;
    }

    @Override
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
    public void setProjectRuleSets(final net.sourceforge.pmd.RuleSets newProjectRuleSets) throws PropertiesException {
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

    @Override
    public boolean isRuleSetStoredInProject() {
        return this.ruleSetStoredInProject;
    }

    @Override
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

    @Override
    public String getRuleSetFile() {
        return StringUtils.isBlank(ruleSetFile) ? PROJECT_RULESET_FILE : ruleSetFile;
    }

    @Override
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

    @Override
    public IWorkingSet getProjectWorkingSet() {
        return this.projectWorkingSet;
    }

    @Override
    public void setProjectWorkingSet(final IWorkingSet projectWorkingSet) {
        LOG.debug("Set working set for project {}: {}", project.getName(),
                projectWorkingSet == null ? "none" : projectWorkingSet.getName());

        this.setNeedRebuild(needRebuild | projectWorkingSet == null ? this.projectWorkingSet != null
                : !projectWorkingSet.equals(this.projectWorkingSet));
        this.projectWorkingSet = projectWorkingSet;
    }

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

    @Override
    public void setNeedRebuild(final boolean needRebuild) {
        LOG.debug("Set needRebuild for project " + project.getName() + ": " + needRebuild);
        this.needRebuild = needRebuild;
    }

    @Override
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

    @Override
    public List<File> getResolvedRuleSetFiles() {
        // Check as project-relative path
        List<File> files = new ArrayList<>();
        for (String ruleSetFile : getRuleSetFile().split(",")) {
            IFile file = project.getFile(ruleSetFile);
            File f = getExistingFileOrNull(file);
            if (f == null) {
                // Check as workspace-relative path
                IWorkspaceRoot workspaceRoot = project.getWorkspace().getRoot();
                try {
                    IFile workspaceFile = workspaceRoot.getFile(new Path(ruleSetFile));
                    f = getExistingFileOrNull(workspaceFile);
                } catch (IllegalArgumentException ignored) {
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
     * Create a project ruleset file from the current configured rules.
     */
    @Override
    public void createDefaultRuleSetFile() throws PropertiesException {
        LOG.info("Create a default rule set file for project " + this.project.getName());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            IRuleSetWriter writer = PMDPlugin.getDefault().getRuleSetWriter();
            
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
                try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
                    file.create(bais, true, null);
                }
            }
        } catch (IOException | WriterException | CoreException e) {
            throw new PropertiesException("Error while creating default ruleset file for project " + this.project.getName(), e);
        }
    }

    @Override
    public boolean isIncludeDerivedFiles() {
        return includeDerivedFiles;
    }

    @Override
    public void setIncludeDerivedFiles(boolean includeDerivedFiles) {
        LOG.debug("Set if derived files should be included: " + includeDerivedFiles);
        this.setNeedRebuild(this.needRebuild | this.includeDerivedFiles != includeDerivedFiles);
        this.includeDerivedFiles = includeDerivedFiles;
    }

    @Override
    public void sync() throws PropertiesException {
        LOG.info("Commit properties for project " + project.getName());
        projectPropertiesManager.storeProjectProperties(this);
    }

    @Override
    public boolean violationsAsErrors() throws PropertiesException {
        return this.violationsAsErrors;
    }

    @Override
    public void setViolationsAsErrors(boolean violationsAsErrors) throws PropertiesException {
        LOG.debug("Set to handle violations as errors: {}", violationsAsErrors);
        this.setNeedRebuild(needRebuild | this.violationsAsErrors != violationsAsErrors);
        this.violationsAsErrors = violationsAsErrors;
    }

    @Override
    public boolean isFullBuildEnabled() throws PropertiesException {
        return fullBuildEnabled;
    }

    @Override
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
     * Provide some help to folks using the debugger and logging.
     */
    @Override
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

    @Override
    public Set<String> getBuildPathExcludePatterns() {
        return buildPathExcludePatterns;
    }

    @Override
    public Set<String> getBuildPathIncludePatterns() {
        return buildPathIncludePatterns;
    }

    @Override
    public ClassLoader getAuxClasspath() {
        try {
            if (project != null && project.hasNature(JavaCore.NATURE_ID)) {
                String projectName = project.getName();
                if (auxclasspath != null && auxclasspath.isModified()) {
                    PMDPlugin.getDefault().logInformation("Classpath of project " + projectName
                            + " changed - recreating it.");
                    try {
                        auxclasspath.close();
                    } catch (IOException ignored) {
                        // ignored
                    }
                    auxclasspath = null;
                }

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
