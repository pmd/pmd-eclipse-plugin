/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.properties;

import org.eclipse.ui.IWorkingSet;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.eclipse.runtime.cmd.AbstractProjectCommand;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.PropertiesException;

import name.herlin.command.CommandException;

/**
 * Save updated project properties. This is a composite command.
 *
 * @author Philippe Herlin
 *
 */
public class UpdateProjectPropertiesCmd extends AbstractProjectCommand {

    private static final long serialVersionUID = 1L;

    // private IProject project;
    private boolean pmdEnabled;
    private IWorkingSet projectWorkingSet;
    private RuleSets projectRuleSets;
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

    /**
     * @see name.herlin.command.AbstractProcessableCommand#execute()
     */
    public void execute() throws CommandException {
        try {
            final IProjectProperties properties = projectProperties();
            properties.setPmdEnabled(pmdEnabled);
            properties.setProjectRuleSets(projectRuleSets);
            properties.setProjectWorkingSet(projectWorkingSet);
            properties.setRuleSetStoredInProject(ruleSetStoredInProject);
            properties.setRuleSetFile(ruleSetFile);
            properties.setIncludeDerivedFiles(includeDerivedFiles);
            properties.setFullBuildEnabled(fullBuildEnabled);
            properties.setViolationsAsErrors(violationsAsErrors);
            properties.sync();
            needRebuild = properties.isNeedRebuild();
            ruleSetFileExists = !properties.isRuleSetFileExist();

        } catch (PropertiesException e) {
            throw new CommandException(e.getMessage(), e);
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

    /**
     * @see name.herlin.command.Command#reset()
     */
    public void reset() {
        setProject(null);
        setPmdEnabled(false);
        setProjectRuleSets(null);
        setRuleSetStoredInProject(false);
        setRuleSetFile(null);
        setIncludeDerivedFiles(false);
        setFullBuildEnabled(true); // made to match static initializer
        setViolationsAsErrors(true); // 10/2010 changed to true to match static initializer
        setTerminated(false);
    }

    /**
     * @see name.herlin.command.Command#isReadyToExecute()
     */
    public boolean isReadyToExecute() {
        return super.isReadyToExecute() && projectRuleSets != null;
    }
}
