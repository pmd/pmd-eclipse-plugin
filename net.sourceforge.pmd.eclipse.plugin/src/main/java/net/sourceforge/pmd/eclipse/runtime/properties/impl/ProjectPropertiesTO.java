/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.properties.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class is a simple data bean to let simply serialize project properties
 * to an XML file (or any).
 * 
 * @author Philippe Herlin
 *
 */
@XmlRootElement(name = "pmd")
@XmlType(propOrder = { "workingSetName", "ruleSetStoredInProject", "ruleSetFile", "excludePatterns", "includePatterns",
    "rules", "includeDerivedFiles", "violationsAsErrors", "fullBuildEnabled" })
public class ProjectPropertiesTO {
    private RuleSpecTO[] rules;
    private String[] excludePatterns;
    private String[] includePatterns;
    private String workingSetName;
    private boolean ruleSetStoredInProject;
    private String ruleSetFile;
    private boolean includeDerivedFiles;
    private boolean violationsAsErrors = true;
    /** set the default to true to match pre-flag behavior */
    private boolean fullBuildEnabled = true;

    /**
     * @return rules an array of RuleSpecTO objects that keep information of
     *         rules selected for the current project
     */
    @XmlElementWrapper(name = "rules")
    @XmlElement(name = "rule")
    public RuleSpecTO[] getRules() {
        return rules;
    }

    /**
     * Set the rules selected for a project
     * 
     * @param rules
     *            an array of RuleSpecTO objects describing each select project
     *            rules.
     */
    public void setRules(final RuleSpecTO[] rules) {
        this.rules = rules;
    }

    /**
     * @return an array of String objects for exclude patterns for the current
     *         project.
     */
    @XmlElementWrapper(name = "excludePatterns")
    @XmlElement(name = "excludePattern")
    public String[] getExcludePatterns() {
        return excludePatterns;
    }

    /**
     * Set the exclude patterns for a project
     * 
     * @param excludePatterns
     *            an array of String objects for exclude patterns for the
     *            current project.
     */
    public void setExcludePatterns(String[] excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    /**
     * @return an array of String objects for include patterns for the current
     *         project.
     */
    @XmlElementWrapper(name = "includePatterns")
    @XmlElement(name = "includePattern")
    public String[] getIncludePatterns() {
        return includePatterns;
    }

    /**
     * Set the include patterns for a project
     * 
     * @param includePatterns
     *            an array of String objects for include patterns for the
     *            current project.
     */
    public void setIncludePatterns(String[] includePatterns) {
        this.includePatterns = includePatterns;
    }

    /**
     * @return ruleSetStoredInProject tells whether the project use a ruleset
     *         stored in the project or the global plugin ruleset.
     */
    @XmlElement(name = "useProjectRuleSet")
    public boolean isRuleSetStoredInProject() {
        return ruleSetStoredInProject;
    }

    /**
     * Tells whether a project must use a ruleset stored in the project or the
     * global project ruleset.
     * 
     * @param ruleSetStoredInProject
     *            see above.
     */
    public void setRuleSetStoredInProject(final boolean ruleSetStoredInProject) {
        this.ruleSetStoredInProject = ruleSetStoredInProject;
    }

    /**
     * @return Returns the rule set file.
     */
    @XmlElement(name = "ruleSetFile")
    public String getRuleSetFile() {
        return ruleSetFile;
    }

    /**
     * @param ruleSetFile
     *            The rule set file.
     */
    public void setRuleSetFile(String ruleSetFile) {
        this.ruleSetFile = ruleSetFile;
    }

    /**
     * @return workingSetName the name of the project workingSet
     */
    @XmlElement(name = "workingSet")
    public String getWorkingSetName() {
        return workingSetName;
    }

    /**
     * Set the project working set name
     * 
     * @param workingSetName
     *            the name of the project working set
     */
    public void setWorkingSetName(final String workingSetName) {
        this.workingSetName = workingSetName;
    }

    /**
     * @return Returns the includeDerivedFiles.
     */
    @XmlElement(name = "includeDerivedFiles")
    public boolean isIncludeDerivedFiles() {
        return this.includeDerivedFiles;
    }

    /**
     * @param includeDerivedFiles
     *            The includeDerivedFiles to set.
     */
    public void setIncludeDerivedFiles(boolean includeDerivedFiles) {
        this.includeDerivedFiles = includeDerivedFiles;
    }

    @XmlElement(name = "violationsAsErrors")
    public boolean isViolationsAsErrors() {
        return violationsAsErrors;
    }

    public void setViolationsAsErrors(boolean violationsAsErrors) {
        this.violationsAsErrors = violationsAsErrors;
    }

    /**
     * syntactic sugar for accessing this field
     * 
     * @param fullBuildEnabled
     *            whether or not we should run at full build
     */
    public void setFullBuildEnabled(boolean fullBuildEnabled) {
        this.fullBuildEnabled = fullBuildEnabled;
    }

    /**
     * syntactic sugar for accessing this field
     * 
     * @return true if we should run at full build
     */
    @XmlElement(name = "fullBuildEnabled")
    public boolean isFullBuildEnabled() {
        return fullBuildEnabled;
    }

}
