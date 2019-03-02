/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.properties;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.cmd.BuildProjectCommand;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectPropertiesManager;
import net.sourceforge.pmd.eclipse.runtime.properties.PropertiesException;
import net.sourceforge.pmd.eclipse.runtime.properties.impl.PropertiesFactoryImpl;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 * This class implements the controler of the Property page
 *
 * @author Philippe Herlin
 *
 */
public class PMDPropertyPageController {
    private static final Logger LOG = Logger.getLogger(PMDPropertyPageController.class);
    private final Shell shell;
    private IProject project;
    private PMDPropertyPageBean propertyPageBean;
    private boolean pmdAlreadyActivated;

    /**
     * Contructor
     *
     * @param shell
     *            the shell from the view the controller is associated
     */
    public PMDPropertyPageController(final Shell shell) {
        super();
        this.shell = shell;
    }

    /**
     * @return Returns the project.
     */
    public IProject getProject() {
        return project;
    }

    /**
     * @param element
     *            The project to set.
     */
    public void setProject(final IProject project) {
        if (project.isAccessible()) {
            this.project = project;
        } else {
            LOG.warn("Couldn't accept project because it is not accessible.");
        }
    }

    /**
     * populates teh property page bean from the loaded properties
     * 
     * @return Returns the propertyPageBean.
     */
    public PMDPropertyPageBean getPropertyPageBean() {
        // assert ((this.project != null) && (this.project.isAccessible()))

        if (this.propertyPageBean == null) {
            LOG.debug("Building a property page bean");
            IProjectProperties properties;

            try {
                properties = PMDPlugin.getDefault().loadProjectProperties(this.project);
            } catch (PropertiesException e) {
                PMDPlugin.getDefault().showError("Error loading project properties. Recreating empty properties.", e);
                IProjectPropertiesManager propertiesManager = PMDPlugin.getDefault().getPropertiesManager();
                properties = new PropertiesFactoryImpl().newProjectProperties(project, propertiesManager);
                try {
                    propertiesManager.storeProjectProperties(properties);
                } catch (PropertiesException e1) {
                    PMDPlugin.getDefault().showError(e.getMessage(), e);
                }
            }

            try {
                propertyPageBean = new PMDPropertyPageBean();
                propertyPageBean.setPmdEnabled(properties.isPmdEnabled());
                propertyPageBean.setProjectWorkingSet(properties.getProjectWorkingSet());
                propertyPageBean.setProjectRuleSets(properties.getProjectRuleSets());
                propertyPageBean.setRuleSetStoredInProject(properties.isRuleSetStoredInProject());
                propertyPageBean.setRuleSetFile(properties.getRuleSetFile());
                propertyPageBean.setIncludeDerivedFiles(properties.isIncludeDerivedFiles());
                propertyPageBean.setFullBuildEnabled(properties.isFullBuildEnabled());
                propertyPageBean.setViolationsAsErrors(properties.violationsAsErrors());
                pmdAlreadyActivated = properties.isPmdEnabled();
            } catch (PropertiesException e) {
                PMDPlugin.getDefault().showError(e.getMessage(), e);
            }
        }

        return this.propertyPageBean;
    }

    /**
     * @return the configured ruleset for the entire workbench
     */
    public RuleSet getAvailableRules() {
        return PMDPlugin.getDefault().getPreferencesManager().getRuleSet();
    }

    /**
     * Process the validation of the properties (OK button pressed)
     *
     * @return always true
     */
    public boolean performOk() {
        // assert ((this.project != null) && (this.project.isAccessible()))

        try {
            checkProjectRuleSetFile();

            // Updates the project properties
            final UpdateProjectPropertiesCmd cmd = new UpdateProjectPropertiesCmd();
            cmd.setProject(project);
            cmd.setPmdEnabled(propertyPageBean.isPmdEnabled());
            cmd.setProjectWorkingSet(propertyPageBean.getProjectWorkingSet());
            cmd.setProjectRuleSets(propertyPageBean.getProjectRuleSets());
            cmd.setRuleSetStoredInProject(propertyPageBean.isRuleSetStoredInProject());
            cmd.setRuleSetFile(propertyPageBean.getRuleSetFile());
            cmd.setIncludeDerivedFiles(propertyPageBean.isIncludeDerivedFiles());
            cmd.setFullBuildEnabled(propertyPageBean.isFullBuildEnabled());
            cmd.setViolationsAsErrors(propertyPageBean.violationsAsErrors());
            cmd.setUserInitiated(true);
            cmd.performExecute();

            // If rebuild is needed, then rebuild the project
            LOG.debug("Updating command terminated, checking whether the project need to be rebuilt");
            if (pmdAlreadyActivated && cmd.isNeedRebuild()) {
                rebuildProject();
            }
        } catch (PropertiesException e) {
            PMDPlugin.getDefault().showError(e.getMessage(), e);
        } catch (RuntimeException e) {
            PMDPlugin.getDefault().showError(e.getMessage(), e);
        }

        return true;
    }

    /**
     * Process a select workingset event
     *
     * @param currentWorkingSet
     *            the working set currently selected of null if none
     * @return the newly selected working set or null if none.
     *
     */
    public IWorkingSet selectWorkingSet(final IWorkingSet currentWorkingSet) {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkingSetManager workingSetManager = workbench.getWorkingSetManager();
        final IWorkingSetSelectionDialog selectionDialog = workingSetManager.createWorkingSetSelectionDialog(this.shell,
                false);
        IWorkingSet selectedWorkingSet = null;

        if (currentWorkingSet != null) {
            selectionDialog.setSelection(new IWorkingSet[] { currentWorkingSet });
        }

        if (selectionDialog.open() == Window.OK) {
            if (selectionDialog.getSelection().length == 0) {
                LOG.info("Deselect working set");
            } else {
                selectedWorkingSet = selectionDialog.getSelection()[0];
                LOG.info("Working set " + selectedWorkingSet.getName() + " selected");
            }
        }

        return selectedWorkingSet;
    }

    /**
     * Perform a full rebuild of the project
     *
     * @param monitor
     *            a progress monitor
     *
     */
    private void rebuildProject() {
        boolean rebuild = MessageDialog.openQuestion(shell, getMessage(StringKeys.QUESTION_TITLE),
                getMessage(StringKeys.QUESTION_REBUILD_PROJECT));

        if (rebuild) {
            LOG.info("Full rebuild of the project " + project.getName());
            try {
                final BuildProjectCommand cmd = new BuildProjectCommand();
                cmd.setProject(project);
                cmd.setUserInitiated(true);
                cmd.performExecute();
            } catch (RuntimeException e) {
                PMDPlugin.getDefault().showError(e.getMessage(), e);
            }
        }
    }

    /**
     * If the user asks to use a project ruleset file, check if it exists. Otherwise, asks the user to create a default
     * one
     *
     */
    private void checkProjectRuleSetFile() throws PropertiesException {
        if (propertyPageBean.isRuleSetStoredInProject()) {
            final IProjectProperties properties = PMDPlugin.getDefault().loadProjectProperties(project);
            if (!properties.isRuleSetFileExist()) {
                createDefaultRuleSetFile();
            }
        }
    }

    /**
     * Create a default ruleset file from the current project ruleset
     *
     */
    private void createDefaultRuleSetFile() throws PropertiesException {
        final boolean create = MessageDialog.openQuestion(shell, getMessage(StringKeys.QUESTION_TITLE),
                getMessage(StringKeys.QUESTION_CREATE_RULESET_FILE));
        if (create) {
            final IProjectProperties properties = PMDPlugin.getDefault().loadProjectProperties(project);
            properties.createDefaultRuleSetFile();
        } else {
            propertyPageBean.setRuleSetStoredInProject(false);
        }
    }

    /**
     * Helper method to shorten message access
     *
     * @param key
     *            a message key
     * @return requested message
     */
    protected String getMessage(final String key) {
        return PMDPlugin.getDefault().getStringTable().getString(key);
    }
}
