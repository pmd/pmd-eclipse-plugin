/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.properties;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IWorkingSet;

import net.sourceforge.pmd.RuleSet;

/**
 * This interface specifies what is the model for the PMD related project
 * properties
 * 
 * @author Philippe Herlin
 *
 */
public interface IProjectProperties {
    /**
     * @return the related project
     */
    IProject getProject();

    /**
     * @return Returns whether PMD is enabled for that project.
     */
    boolean isPmdEnabled() throws PropertiesException;

    /**
     * @param pmdEnabled
     *            Enable or disable PMD for that project.
     */
    void setPmdEnabled(boolean pmdEnabled) throws PropertiesException;

    /**
     * Only returns the first ruleset. To access all rulesets,
     * use {@link #getProjectRuleSetList()}.
     * @return Returns the first rule set in the project rulesets
     */
    RuleSet getProjectRuleSet() throws PropertiesException;

    /**
     * Sets a single project ruleset.
     * To use multiple rulesets, see {@link #setProjectRuleSetList(List)}.
     * @param projectRuleSet
     *            The project Rule Set to set.
     */
    void setProjectRuleSet(RuleSet projectRuleSet) throws PropertiesException;

    /**
     * @param projectRuleSets
     *            The project Rule Sets to set.
     * @deprecated Use {@link #setProjectRuleSetList(List)}
     */
    @Deprecated
    void setProjectRuleSets(net.sourceforge.pmd.RuleSets projectRuleSets) throws PropertiesException;
    
    void setProjectRuleSetList(List<RuleSet> rulesets) throws PropertiesException;

    /**
     * @return Returns the project Rule Sets.
     * @deprecated Use {@link #getProjectRuleSetList()}
     */
    @Deprecated
    net.sourceforge.pmd.RuleSets getProjectRuleSets() throws PropertiesException;

    List<RuleSet> getProjectRuleSetList() throws PropertiesException;

    /**
     * @return Returns the whether the project rule set is stored as a file
     *         inside the project.
     */
    boolean isRuleSetStoredInProject() throws PropertiesException;

    /**
     * @param ruleSetStoredInProject
     *            Specify whether the rule set is stored in the project.
     */
    void setRuleSetStoredInProject(boolean ruleSetStoredInProject) throws PropertiesException;

    /**
     * @return Returns the rule set file.
     */
    String getRuleSetFile() throws PropertiesException;

    /**
     * @param ruleSetFile
     *            The rule set file.
     */
    void setRuleSetFile(String ruleSetFile) throws PropertiesException;

    /**
     * @return Returns the resolved RuleSet File suitable for loading a rule
     *         set.
     * @deprecated use {@link #getResolvedRuleSetFiles()} instead
     */
    @Deprecated
    File getResolvedRuleSetFile() throws PropertiesException;

    /**
     * @return Returns the resolved RuleSet Files suitable for loading multiple
     * rulesets.
     */
    List<File> getResolvedRuleSetFiles() throws PropertiesException;

    /**
     * @return Returns the project Working Set.
     */
    IWorkingSet getProjectWorkingSet() throws PropertiesException;

    /**
     * @param projectWorkingSet
     *            The project Working Set to set.
     */
    void setProjectWorkingSet(IWorkingSet projectWorkingSet) throws PropertiesException;

    /**
     * @return whether the project needs to be rebuilt.
     */
    boolean isNeedRebuild() throws PropertiesException;

    /**
     * Let force the rebuild state of a project.
     */
    void setNeedRebuild(boolean needRebuild) throws PropertiesException;

    /**
     * @return in case the rule set is stored inside the project, whether the
     *         ruleset file exists.
     */
    boolean isRuleSetFileExist() throws PropertiesException;

    /**
     * Create a project ruleset file from the current configured rules
     *
     */
    void createDefaultRuleSetFile() throws PropertiesException;

    /**
     * @return whether derived files should be checked
     */
    boolean isIncludeDerivedFiles() throws PropertiesException;

    /**
     * @param excludeDerivedFiles
     *            whether derived files should be checked
     */
    void setIncludeDerivedFiles(boolean excludeDerivedFiles) throws PropertiesException;

    /**
     * @return whether high priority violations should be handled as Eclipse
     *         errors
     */
    boolean violationsAsErrors() throws PropertiesException;

    /**
     * @param violationsAsErrors
     *            whether high priority violations should be handled as Eclipse
     *            errors
     */
    void setViolationsAsErrors(boolean violationsAsErrors) throws PropertiesException;

    /**
     * @return Returns whether PMD should be run for full build for that
     *         project.
     */
    boolean isFullBuildEnabled() throws PropertiesException;

    /**
     * @param pmdEnabled
     *            Enable or disable PMD for full build for that project.
     */
    void setFullBuildEnabled(boolean fullBuildEnabled) throws PropertiesException;

    /**
     * Synchronize the properties with the persistant store
     *
     */
    void sync() throws PropertiesException;

    /**
     * The exclude patterns determined from the project's build path. Note: not
     * persisted.
     * 
     * @return exclude patterns
     */
    Set<String> getBuildPathExcludePatterns();

    /**
     * The include patterns determined from the project's build path. Note: not
     * persisted.
     * 
     * @return include patterns
     */
    Set<String> getBuildPathIncludePatterns();

    /**
     * Determines the auxiliary classpath needed for type resolution.
     * The classloader is cached and used for all PMD executions for the same project.
     * The classloader is not stored to the project properties file.
     * 
     * @return the classpath or <code>null</code> if the project is not a java project
     */
    ClassLoader getAuxClasspath();
}
