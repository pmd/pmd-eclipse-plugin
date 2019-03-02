/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.properties;

import org.eclipse.ui.IWorkingSet;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSets;

/**
 * This class is a bean that hold the property page data. It acts as the model in the MVC paradigm.
 * 
 * @author Philippe Herlin
 *
 */
public class PMDPropertyPageBean {
    private boolean pmdEnabled;
    private IWorkingSet projectWorkingSet;
    private RuleSets projectRuleSets;
    private boolean ruleSetStoredInProject;
    private String ruleSetFile;
    private boolean includeDerivedFiles;
    private boolean fullBuildEnabled = true;
    private boolean violationsAsErrors = true;

    /**
     * @return Returns the pmdEnabled.
     */
    public boolean isPmdEnabled() {
        return pmdEnabled;
    }

    /**
     * @param pmdEnabled
     *            The pmdEnabled to set.
     */
    public void setPmdEnabled(final boolean pmdEnabled) {
        this.pmdEnabled = pmdEnabled;
    }

    /**
     * Gets the first ruleset
     * @return
     */
    public RuleSet getProjectRuleSet() {
        return projectRuleSets.getAllRuleSets()[0];
    }

    /**
     * @return Returns the projectRuleSet.
     */
    public RuleSets getProjectRuleSets() {
        return projectRuleSets;
    }

    public void setProjectRuleSet(final RuleSet projectRuleSet) {
        this.projectRuleSets = new RuleSets(projectRuleSet);
    }

    /**
     * @param projectRuleSet
     *            The projectRuleSet to set.
     */
    public void setProjectRuleSets(final RuleSets projectRuleSets) {
        this.projectRuleSets = projectRuleSets;
    }

    /**
     * @return Returns the ruleSetStoredInProject.
     */
    public boolean isRuleSetStoredInProject() {
        return ruleSetStoredInProject;
    }

    /**
     * @param ruleSetStoredInProject
     *            The ruleSetStoredInProject to set.
     */
    public void setRuleSetStoredInProject(final boolean ruleSetStoredInProject) {
        this.ruleSetStoredInProject = ruleSetStoredInProject;
    }

    /**
     * @return Returns the ruleSetFile.
     */
    public String getRuleSetFile() {
        return ruleSetFile;
    }

    /**
     * @param ruleSetFile
     *            The ruleSetFile to set.
     */
    public void setRuleSetFile(String ruleSetFile) {
        this.ruleSetFile = ruleSetFile;
    }

    /**
     * @return Returns the projectWorkingSet.
     */
    public IWorkingSet getProjectWorkingSet() {
        return projectWorkingSet;
    }

    /**
     * @param projectWorkingSet
     *            The projectWorkingSet to set.
     */
    public void setProjectWorkingSet(final IWorkingSet selectedWorkingSet) {
        this.projectWorkingSet = selectedWorkingSet;
    }

    /**
     * @return Returns the includeDerivedFiles.
     */
    public boolean isIncludeDerivedFiles() {
        return includeDerivedFiles;
    }

    /**
     * @param includeDerivedFiles
     *            The includeDerivedFiles to set.
     */
    public void setIncludeDerivedFiles(boolean includeDerivedFiles) {
        this.includeDerivedFiles = includeDerivedFiles;
    }

    /**
     * @return should we run at full build
     */
    public boolean isFullBuildEnabled() {
        return fullBuildEnabled;
    }

    /**
     * 
     * @param fullBuildEnabled
     *            run at full build
     */
    public void setFullBuildEnabled(boolean fullBuildEnabled) {
        this.fullBuildEnabled = fullBuildEnabled;
    }

    /**
     * @return Returns the violationsAsErrors.
     */
    public boolean violationsAsErrors() {
        return violationsAsErrors;
    }

    /**
     * @param setViolationsAsErrors
     *            The setViolationsAsErrors to set.
     */
    public void setViolationsAsErrors(boolean violationsAsErrors) {
        this.violationsAsErrors = violationsAsErrors;
    }
}
