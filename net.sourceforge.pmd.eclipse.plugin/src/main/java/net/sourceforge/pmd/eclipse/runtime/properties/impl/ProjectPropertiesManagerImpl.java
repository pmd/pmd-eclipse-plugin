/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.properties.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.builder.PMDNature;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectPropertiesManager;
import net.sourceforge.pmd.eclipse.runtime.properties.PropertiesException;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;

/**
 * This class manages the persistence of the ProjectProperies information structure
 *
 * @author Philippe Herlin
 *
 */
public class ProjectPropertiesManagerImpl implements IProjectPropertiesManager {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectPropertiesManagerImpl.class);

    private static final String PROPERTIES_FILE = ".pmd";

    private final Map<IProject, IProjectProperties> projectsProperties = new HashMap<IProject, IProjectProperties>();

    private static final JAXBContext JAXB_CONTEXT = initJaxbContext();

    private static JAXBContext initJaxbContext() {
        try {
            return JAXBContext.newInstance(ProjectPropertiesTO.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load a project properties
     *
     * @param project
     *            a project
     */
    @Override
    public IProjectProperties loadProjectProperties(final IProject project) throws PropertiesException {
        LOG.debug("Loading project properties for project " + project.getName());
        try {
            IProjectProperties projectProperties = this.projectsProperties.get(project);
            if (projectProperties == null) {
                LOG.debug("Creating new poject properties for " + project.getName());
                projectProperties = new PropertiesFactoryImpl().newProjectProperties(project, this);
                final ProjectPropertiesTO to = readProjectProperties(project);
                fillProjectProperties(projectProperties, to);
                this.projectsProperties.put(project, projectProperties);
            }

            // if the ruleset is stored in the project always reload it
            if (projectProperties.isRuleSetStoredInProject()) {
                loadRuleSetFromProject(projectProperties);
            } else {
                // else resynchronize the ruleset
                final boolean needRebuild = synchronizeRuleSet(projectProperties);
                projectProperties.setNeedRebuild(projectProperties.isNeedRebuild() || needRebuild);
            }

            return projectProperties;

        } catch (CoreException e) {
            throw new PropertiesException(
                    "Core Exception when loading project properties for project " + project.getName(), e);
        }
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.properties.IProjectPropertiesManager#storeProjectProperties(net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties)
     */
    @Override
    public void storeProjectProperties(IProjectProperties projectProperties) throws PropertiesException {
        LOG.debug("Storing project properties for project " + projectProperties.getProject().getName());
        try {
            if (projectProperties.isPmdEnabled()) {
                PMDNature.addPMDNature(projectProperties.getProject(), null);
            } else {
                PMDNature.removePMDNature(projectProperties.getProject(), null);
            }

            writeProjectProperties(projectProperties.getProject(), fillTransferObject(projectProperties));
            projectsProperties.put(projectProperties.getProject(), projectProperties);

        } catch (CoreException e) {
            throw new PropertiesException("Core Exception when storing project properties for project "
                    + projectProperties.getProject().getName(), e);
        }

    }

    @Override
    public void removeProjectProperties(IProject project) {
        this.projectsProperties.remove(project);
    }

    /**
     * Load the project rule set from the project ruleset
     *
     */
    private void loadRuleSetFromProject(IProjectProperties projectProperties) throws PropertiesException {
        if (projectProperties.isRuleSetFileExist() && projectProperties.isNeedRebuild()) {
            LOG.debug("Loading ruleset from project ruleset file: " + projectProperties.getRuleSetFile());
            try {
                final RuleSetFactory factory = new RuleSetFactory();
                RuleSets allRulesets = new RuleSets();
                for (final File ruleSetFile : projectProperties.getResolvedRuleSetFiles()) {
                    RuleSet ruleSet = factory.createRuleSets(ruleSetFile.getPath()).getAllRuleSets()[0];
                    allRulesets.addRuleSet(ruleSet);
                }
                projectProperties.setProjectRuleSets(allRulesets);
                projectProperties.setNeedRebuild(false);
            } catch (RuleSetNotFoundException e) {
                PMDPlugin.getDefault()
                        .logError("Project RuleSet cannot be loaded for project "
                                + projectProperties.getProject().getName() + " using RuleSet file name "
                                + projectProperties.getRuleSetFile() + ". Using the rules from properties.", e);
            }
        }
    }

    public ProjectPropertiesTO convertProjectPropertiesFromString(String properties) {
        try {
            Source source = new StreamSource(new StringReader(properties));
            JAXBElement<ProjectPropertiesTO> element = JAXB_CONTEXT.createUnmarshaller().unmarshal(source,
                    ProjectPropertiesTO.class);
            return element.getValue();
        } catch (JAXBException e) {
            throw new DataBindingException(e);
        }
    }

    /**
     * Read a project properties from properties file
     *
     * @param project
     *            a project
     */
    private ProjectPropertiesTO readProjectProperties(final IProject project) throws PropertiesException {
        ProjectPropertiesTO projectProperties = null;
        try {

            final IFile propertiesFile = project.getFile(PROPERTIES_FILE);
            if (propertiesFile.exists() && propertiesFile.isAccessible()) {
                String properties = IOUtils.toString(propertiesFile.getContents(), StandardCharsets.UTF_8);
                projectProperties = convertProjectPropertiesFromString(properties);
            }

            return projectProperties;

        } catch (IOException e) {
            throw new PropertiesException(e);
        } catch (CoreException e) {
            throw new PropertiesException(e);
        } catch (DataBindingException e) {
            throw new PropertiesException(e);
        }
    }

    /**
     * Fill a properties information structure from a transfer object
     *
     * @param projectProperties
     *            a project properties data structure
     * @param to
     *            a project properties transfer object
     */
    private void fillProjectProperties(IProjectProperties projectProperties, ProjectPropertiesTO to)
            throws PropertiesException, CoreException {
        if (to == null) {
            LOG.info("Project properties not found. Use default.");
        } else {
            final IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
            projectProperties.setProjectWorkingSet(workingSetManager.getWorkingSet(to.getWorkingSetName()));

            projectProperties.setRuleSetFile(to.getRuleSetFile());
            projectProperties.setRuleSetStoredInProject(to.isRuleSetStoredInProject());
            projectProperties.setPmdEnabled(projectProperties.getProject().hasNature(PMDNature.PMD_NATURE));
            projectProperties.setIncludeDerivedFiles(to.isIncludeDerivedFiles());
            projectProperties.setViolationsAsErrors(to.isViolationsAsErrors());
            projectProperties.setFullBuildEnabled(to.isFullBuildEnabled());

            if (to.isRuleSetStoredInProject()) {
                loadRuleSetFromProject(projectProperties);
            } else {
                setRuleSetFromProperties(projectProperties, to.getRules());
            }

            LOG.debug("Project properties loaded");
        }
    }

    /**
     * Set the rule set from rule specs found in properties file
     *
     * @param rules
     *            array of selected rules
     */
    private void setRuleSetFromProperties(IProjectProperties projectProperties, RuleSpecTO[] rules)
            throws PropertiesException {
        final RuleSet pluginRuleSet = PMDPlugin.getDefault().getPreferencesManager().getRuleSet();

        // de-duplicate rules
        Set<String> ruleNamesToAdd = new HashSet<>();
        if (rules != null) {
            for (RuleSpecTO rule : rules) {
                if (!ruleNamesToAdd.add(rule.getName())) {
                    PMDPlugin.getDefault()
                            .logInformation("Duplicated Rule found: " + rule.getName() + ". This rule will be ignored.");
                    LOG.debug("Duplicated Rule found: " + rule.getName() + ". This rule will be ignored.");
                }
            }
        }

        List<Rule> rulesToAdd = new ArrayList<Rule>();
        for (String ruleName : ruleNamesToAdd) {
            Rule rule = pluginRuleSet.getRuleByName(ruleName);
            if (rule != null) {
                rulesToAdd.add(rule);
            } else {
                PMDPlugin.getDefault()
                        .logInformation("The rule " + ruleName + " could not be found. The rule will be ignored.");
                LOG.debug("The rule " + ruleName + " could not be found. The rule will be ignored.");
            }
        }

        RuleSet ruleSet = RuleSetUtil.newEmpty(RuleSetUtil.DEFAULT_RULESET_NAME,
                RuleSetUtil.DEFAULT_RULESET_DESCRIPTION);
        ruleSet = RuleSetUtil.addRules(ruleSet, rulesToAdd);
        ruleSet = RuleSetUtil.setExcludePatterns(ruleSet, pluginRuleSet.getExcludePatterns());
        ruleSet = RuleSetUtil.setIncludePatterns(ruleSet, pluginRuleSet.getIncludePatterns());
        projectProperties.setProjectRuleSet(ruleSet);
    }

    public String convertProjectPropertiesToString(ProjectPropertiesTO projectProperties) {
        try {
            Marshaller marshaller = JAXB_CONTEXT.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

            StringWriter writer = new StringWriter();
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            marshaller.marshal(projectProperties, writer);
            writer.write("\n");

            return writer.toString();
        } catch (JAXBException e) {
            throw new DataBindingException(e);
        }
    }

    /**
     * Save project properties
     *
     * @param project
     *            a project
     * @param projectProperties
     *            the project properties to save
     * @param monitor
     *            a progress monitor
     * @throws DAOException
     */
    private void writeProjectProperties(final IProject project, final ProjectPropertiesTO projectProperties)
            throws PropertiesException {
        try {
            String writer = convertProjectPropertiesToString(projectProperties);

            final IFile propertiesFile = project.getFile(PROPERTIES_FILE);
            if (propertiesFile.exists() && propertiesFile.isAccessible()) {
                propertiesFile.setContents(new ByteArrayInputStream(writer.getBytes()), false, false, null);
            } else {
                propertiesFile.create(new ByteArrayInputStream(writer.getBytes()), false, null);
            }
        } catch (CoreException e) {
            throw new PropertiesException(e);
        }
    }

    /**
     * Fill in a transfer object from a project properties information structure
     *
     * @throws DAOException
     */
    private ProjectPropertiesTO fillTransferObject(IProjectProperties projectProperties) throws PropertiesException {
        final ProjectPropertiesTO bean = new ProjectPropertiesTO();
        bean.setRuleSetStoredInProject(projectProperties.isRuleSetStoredInProject());
        bean.setRuleSetFile(projectProperties.getRuleSetFile());
        bean.setWorkingSetName(projectProperties.getProjectWorkingSet() == null ? null
                : projectProperties.getProjectWorkingSet().getName());
        bean.setIncludeDerivedFiles(projectProperties.isIncludeDerivedFiles());
        bean.setViolationsAsErrors(projectProperties.violationsAsErrors());
        bean.setFullBuildEnabled(projectProperties.isFullBuildEnabled());

        if (!projectProperties.isRuleSetStoredInProject()) {
            final RuleSets ruleSets = projectProperties.getProjectRuleSets();
            final List<RuleSpecTO> rules = new ArrayList<RuleSpecTO>();
            List<String> excludePatterns = new ArrayList<String>();
            List<String> includePatterns = new ArrayList<String>();

            for (RuleSet ruleSet : ruleSets.getAllRuleSets()) {
                for (Rule rule : ruleSets.getAllRules()) {
                    rules.add(new RuleSpecTO(rule.getName(), rule.getRuleSetName())); // NOPMD:AvoidInstantiatingObjectInLoop
                }
                excludePatterns.addAll(ruleSet.getExcludePatterns());
                includePatterns.addAll(ruleSet.getIncludePatterns());
            }

            bean.setRules(rules.toArray(new RuleSpecTO[rules.size()]));
            bean.setExcludePatterns(excludePatterns.toArray(new String[0]));
            bean.setIncludePatterns(includePatterns.toArray(new String[0]));
        }
        return bean;
    }

    /**
     * Check the project ruleset against the plugin ruleset and synchronize if necessary
     *
     * @return true if the project ruleset has changed.
     *
     */
    private boolean synchronizeRuleSet(IProjectProperties projectProperties) throws PropertiesException {
        LOG.debug("Synchronizing the project ruleset with the plugin ruleset");
        final RuleSet pluginRuleSet = PMDPlugin.getDefault().getPreferencesManager().getRuleSet();
        final RuleSets projectRuleSets = projectProperties.getProjectRuleSets();
        boolean flChanged = false;

        if (!projectRuleSets.getAllRules().equals(pluginRuleSet.getRules())) {
            LOG.debug("The project ruleset is different from the plugin ruleset; synchronizing.");

            // 1-If rules have been deleted from preferences, delete them also
            // from the project ruleset
            // 2-For all other rules, replace the current one by the plugin one
            RuleSet ruleset = projectRuleSets.getAllRuleSets()[0];
            RuleSet newRuleSet = RuleSetUtil.newEmpty(ruleset.getName(), ruleset.getDescription());
            List<Rule> newRules = new ArrayList<Rule>();
            List<Rule> haystack = new ArrayList<Rule>(pluginRuleSet.getRules());
            for (RuleSet projectRuleSet : projectRuleSets.getAllRuleSets()) {
                for (Rule projectRule : projectRuleSet.getRules()) {
                    final Rule pluginRule = RuleSetUtil.findSameRule(haystack, projectRule);
                    if (pluginRule == null) {
                        LOG.debug(
                                "The rule " + projectRule.getName() + " is not defined in the plugin ruleset. Remove it.");
                    } else {
                        // log.debug("Keeping rule " + projectRule.getName());
                        newRules.add(pluginRule);
                        // consider the found rule as handled - there is no need, to
                        // find the same rule twice.
                        haystack.remove(pluginRule);
                    }
                }
            }
            // build a new ruleset based on the collected rules
            newRuleSet = RuleSetUtil.addRules(newRuleSet, newRules);

            if (!newRuleSet.getRules().equals(projectRuleSets.getAllRules())) {
                LOG.info("Set the project ruleset according to preferences.");
                projectProperties.setProjectRuleSet(newRuleSet);
                flChanged = true;
            }

            LOG.debug("Ruleset for project " + projectProperties.getProject().getName() + " is now synchronized. "
                    + (flChanged ? "Ruleset has changed" : "Ruleset has not changed"));
        }

        return flChanged;
    }
}
