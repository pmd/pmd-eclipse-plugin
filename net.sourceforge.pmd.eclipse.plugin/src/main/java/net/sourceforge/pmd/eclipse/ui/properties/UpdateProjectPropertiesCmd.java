/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.properties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.ui.IWorkingSet;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.eclipse.runtime.cmd.AbstractProjectCommand;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.PropertiesException;

/**
 * Save updated project properties. This is a composite command.
 *
 * @author Philippe Herlin
 *
 */
public class UpdateProjectPropertiesCmd extends AbstractProjectCommand {

    // private IProject project;
    private boolean pmdEnabled;
    private IWorkingSet projectWorkingSet;
    private List<RuleSet> projectRuleSets;
    private boolean ruleSetStoredInProject;
    private String ruleSetFile;
    private boolean needRebuild;
    private boolean ruleSetFileExists;
    private boolean includeDerivedFiles;
    private boolean fullBuildEnabled = true;
    private boolean violationsAsErrors = true;

    /**
     * Default constructor. Initializes command attributes
     *
     */
    public UpdateProjectPropertiesCmd() {
        super("UpdateProjectProperties", "Update a project PMD specific properties.");
        setReadOnly(false);
        setOutputProperties(true);
        setTerminated(false);
    }

    public void execute() {
        try {
            final IProjectProperties properties = projectProperties();
            properties.setPmdEnabled(pmdEnabled);
            properties.setProjectRuleSetList(projectRuleSets);
            properties.setProjectWorkingSet(projectWorkingSet);
            // ruleSetFile has to be set before ruleSetStoredInProject!
            properties.setRuleSetFile(ruleSetFile);
            properties.setRuleSetStoredInProject(ruleSetStoredInProject);
            properties.setIncludeDerivedFiles(includeDerivedFiles);
            properties.setFullBuildEnabled(fullBuildEnabled);
            properties.setViolationsAsErrors(violationsAsErrors);
            properties.sync();
            needRebuild = properties.isNeedRebuild();
            ruleSetFileExists = !properties.isRuleSetFileExist();

        } catch (PropertiesException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            setTerminated(true);
        }
    }

    /**
     * @param pmdEnabled
     *            The pmdEnabled to set.
     */
    public void setPmdEnabled(final boolean pmdEnabled) {
        this.pmdEnabled = pmdEnabled;
    }

    public void setProjectRuleSet(final RuleSet projectRuleSet) {
        this.projectRuleSets = Collections.singletonList(projectRuleSet);
    }

    /**
     * @param projectRuleSet
     *            The projectRuleSet to set.
     * @deprecated Use {@link #setProjectRuleSetList(List)}
     */
    @Deprecated
    public void setProjectRuleSets(final RuleSets projectRuleSets) {
        this.projectRuleSets = Arrays.asList(projectRuleSets.getAllRuleSets());
    }

    public void setProjectRuleSetList(List<RuleSet> projectRuleSets) {
        this.projectRuleSets = projectRuleSets;
    }

    /**
     * @param projectWorkingSet
     *            The projectWorkingSet to set.
     */
    public void setProjectWorkingSet(final IWorkingSet projectWorkingSet) {
        this.projectWorkingSet = projectWorkingSet;
    }

    /**
     * @param ruleSetStoredInProject
     *            The ruleSetStoredInProject to set.
     */
    public void setRuleSetStoredInProject(final boolean ruleSetStoredInProject) {
        this.ruleSetStoredInProject = ruleSetStoredInProject;
    }

    /**
     * @param ruleSetFile
     *            The ruleSetFile to set.
     */
    public void setRuleSetFile(String ruleSetFile) {
        this.ruleSetFile = ruleSetFile;
    }

    /**
     * @param includeDerivedFiles
     *            The includeDerivedFiles to set.
     */
    public void setIncludeDerivedFiles(boolean includeDerivedFiles) {
        this.includeDerivedFiles = includeDerivedFiles;
    }

    /**
     * 
     * @param fullBuildEnabled
     *            run at full build setter
     */
    public void setFullBuildEnabled(boolean fullBuildEnabled) {
        this.fullBuildEnabled = fullBuildEnabled;
    }

    /**
     * @param violationsAsErrors
     *            The violationsAsErrors to set.
     */
    public void setViolationsAsErrors(boolean violationsAsErrors) {
        this.violationsAsErrors = violationsAsErrors;
    }

    /**
     * @return Returns the needRebuild.
     */
    public boolean isNeedRebuild() {
        return needRebuild && fullBuildEnabled;
    }

    /**
     * @return Returns the ruleSetFileExists.
     */
    public boolean isRuleSetFileExists() {
        return ruleSetFileExists;
    }

    public void reset() {
        setProject(null);
        setPmdEnabled(false);
        setProjectRuleSetList(null);
        setRuleSetStoredInProject(false);
        setRuleSetFile(null);
        setIncludeDerivedFiles(false);
        setFullBuildEnabled(true); // made to match static initializer
        setViolationsAsErrors(true); // 10/2010 changed to true to match static initializer
        setTerminated(false);
    }

    public boolean isReadyToExecute() {
        return super.isReadyToExecute() && projectRuleSets != null;
    }
}
