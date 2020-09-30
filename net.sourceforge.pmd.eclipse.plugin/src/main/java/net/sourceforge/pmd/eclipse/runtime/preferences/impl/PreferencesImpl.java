/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.preferences.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.eclipse.core.IRuleSetManager;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferencesManager;
import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptor;

/**
 * Implements the preferences information structure
 * 
 * @author Herlin
 *
 */

class PreferencesImpl implements IPreferences {

    private Map<String, Boolean> booleansById = new HashMap<String, Boolean>();

    private IPreferencesManager preferencesManager;
    private boolean projectBuildPathEnabled;
    private boolean pmdPerspectiveEnabled;
    private boolean pmdViolationsOverviewEnabled;
    private boolean pmdViolationsOutlineEnabled;
    private boolean checkAfterSaveEnabled;
    private boolean useCustomPriorityNames;
    private int maxViolationsPerFilePerRule;
    private boolean determineFiletypesAutomatically;
    private String reviewAdditionalComment;
    private boolean reviewPmdStyleEnabled;
    private int minTileSize;
    private String logFileName;
    private String logLevel;
    private boolean globalRuleManagement;
    private Set<String> activeRuleNames = new HashSet<String>();
    private Set<String> activeRendererNames = new HashSet<String>();
    private Set<String> activeExclusionPatterns = new HashSet<String>();
    private Set<String> activeInclusionPatterns = new HashSet<String>();

    private Map<RulePriority, PriorityDescriptor> uiDescriptorsByPriority = new HashMap<RulePriority, PriorityDescriptor>(
            5);

    private int tableFraction;
    private Set<String> hiddenColumnIds;
    private boolean sortDirectionUp;
    private String groupingColumn;
    private Set<String> selectedRuleNames;
    private int selectedPropertyTab;

    /**
     * Is constructed from a preferences manager
     * 
     * @param preferencesManager
     */
    PreferencesImpl(IPreferencesManager preferencesManager) {
        super();
        this.preferencesManager = preferencesManager;
    }

    public boolean boolFor(String prefId) {
        Boolean value = booleansById.get(prefId);
        if (value == null) {
            throw new IllegalArgumentException("Unknown pref id: " + prefId);
        }
        return value;
    }

    public void boolFor(String prefId, boolean newValue) {
        booleansById.put(prefId, newValue);
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#isProjectBuildPathEnabled()
     */
    public boolean isProjectBuildPathEnabled() {
        return projectBuildPathEnabled;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#setProjectBuildPathEnabled(boolean)
     */
    public void setProjectBuildPathEnabled(boolean projectBuildPathEnabled) {
        this.projectBuildPathEnabled = projectBuildPathEnabled;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#isPmdPerspectiveEnabled()
     */
    public boolean isPmdPerspectiveEnabled() {
        return pmdPerspectiveEnabled;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#isCheckCodeAfterSaveEnabled()
     */
    public boolean isCheckAfterSaveEnabled() {
        return checkAfterSaveEnabled;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#isCheckCodeAfterSaveEnabled()
     */
    public void isCheckAfterSaveEnabled(boolean flag) {
        checkAfterSaveEnabled = flag;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#setPmdPerspectiveEnabled(boolean)
     */
    public void setPmdPerspectiveEnabled(boolean pmdPerspectiveEnabled) {
        this.pmdPerspectiveEnabled = pmdPerspectiveEnabled;
    }
    
    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#setPmdViolationsOverviewEnabled(boolean)
     */
    public void setPmdViolationsOverviewEnabled(boolean pmdViolationsOverviewEnabled) {
        this.pmdViolationsOverviewEnabled = pmdViolationsOverviewEnabled;
    }
    

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#getMaxViolationsPerFilePerRule()
     */
    public int getMaxViolationsPerFilePerRule() {
        return maxViolationsPerFilePerRule;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#setMaxViolationsPerFilePerRule(int)
     */
    public void setMaxViolationsPerFilePerRule(int maxViolationPerFilePerRule) {
        this.maxViolationsPerFilePerRule = maxViolationPerFilePerRule;
    }

    @Override
    public boolean isDetermineFiletypesAutomatically() {
        return determineFiletypesAutomatically;
    }

    @Override
    public void setDetermineFiletypesAutomatically(boolean determineFiletypesAutomatically) {
        this.determineFiletypesAutomatically = determineFiletypesAutomatically;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#getReviewAdditionalComment()
     */
    public String getReviewAdditionalComment() {
        return reviewAdditionalComment;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#setReviewAdditionalComment(java.lang.String)
     */
    public void setReviewAdditionalComment(String reviewAdditionalComment) {
        this.reviewAdditionalComment = reviewAdditionalComment;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#isReviewPmdStyleEnabled()
     */
    public boolean isReviewPmdStyleEnabled() {
        return reviewPmdStyleEnabled;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#setReviewPmdStyleEnabled(boolean)
     */
    public void setReviewPmdStyleEnabled(boolean reviewPmdStyleEnabled) {
        this.reviewPmdStyleEnabled = reviewPmdStyleEnabled;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#getMinTileSize()
     */
    public int getMinTileSize() {
        return minTileSize;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#setMinTileSize(int)
     */
    public void setMinTileSize(int minTileSize) {
        this.minTileSize = minTileSize;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#getLogFileName()
     */
    public String getLogFileName() {
        return logFileName;
    }

    @Override
    public String getLogLevelName() {
        return logLevel;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#setLogFileName(java.lang.String)
     */
    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    @Override
    public void setLogLevel(String level) {
        logLevel = level;
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences#sync()
     */
    public void sync() {
        preferencesManager.storePreferences(this);
    }

    public boolean getGlobalRuleManagement() {
        return globalRuleManagement;
    }

    public void setGlobalRuleManagement(boolean b) {
        globalRuleManagement = b;
    }

    public boolean isActive(String ruleName) {
        return activeRuleNames.contains(ruleName);
    }

    public boolean isActiveRenderer(String rendererName) {
        return activeRendererNames.contains(rendererName);
    }

    public void isActive(String ruleName, boolean isActive) {
        if (isActive) {
            activeRuleNames.add(ruleName);
        } else {
            activeRuleNames.remove(ruleName);
        }
    }

    public Set<String> getActiveRuleNames() {
        return activeRuleNames;
    }

    /**
     * Returns a comma-separated list of rules that are active by default. This
     * contains all default rules of PMD.
     */
    public String getDefaultActiveRules() {
        StringBuilder rules = new StringBuilder();
        IRuleSetManager ruleSetManager = PMDPlugin.getDefault().getRuleSetManager();
        for (RuleSet ruleSet : ruleSetManager.getDefaultRuleSets()) {
            for (Rule rule : ruleSet.getRules()) {
                if (rules.length() > 0) {
                    rules.append(',');
                }
                rules.append(rule.getName());
            }
        }
        return rules.toString();
    }

    public void setActiveRuleNames(Set<String> ruleNames) {
        activeRuleNames = ruleNames;
    }

    public Set<String> activeExclusionPatterns() {
        return activeExclusionPatterns;
    }

    public void activeExclusionPatterns(Set<String> patterns) {
        activeExclusionPatterns = patterns;
    }

    public Set<String> activeInclusionPatterns() {
        return activeInclusionPatterns;
    }

    public void activeInclusionPatterns(Set<String> patterns) {
        activeInclusionPatterns = patterns;
    }

    public void setPriorityDescriptor(RulePriority priority, PriorityDescriptor pd) {
        uiDescriptorsByPriority.put(priority, pd);
    }

    public PriorityDescriptor getPriorityDescriptor(RulePriority priority) {
        return uiDescriptorsByPriority.get(priority);
    }

    public boolean useCustomPriorityNames() {
        return useCustomPriorityNames;
    }

    public void useCustomPriorityNames(boolean flag) {
        useCustomPriorityNames = flag;
    }

    public Set<String> activeReportRenderers() {
        return activeRendererNames;
    }
    
    @Override
    public void activeReportRenderers(Set<String> names) {
        activeRendererNames = names;
    }

    @Override
    public boolean isPmdViolationsOverviewEnabled() {
        return pmdViolationsOverviewEnabled;
    }

    @Override
    public boolean isPmdViolationsOutlineEnabled() {
        return pmdViolationsOutlineEnabled;
    }

    @Override
    public void setPmdViolationsOutlineEnabled(boolean pmdViolationsOutlineEnabled) {
        this.pmdViolationsOutlineEnabled = pmdViolationsOutlineEnabled;
    }

    @Override
    public int tableFraction() {
        return tableFraction;
    }

    @Override
    public void tableFraction(int aFraction) {
        this.tableFraction = aFraction;
    }

    @Override
    public Set<String> getHiddenColumnIds() {
        return hiddenColumnIds;
    }

    @Override
    public void setHiddenColumnIds(Set<String> names) {
        hiddenColumnIds = names;
    }

    @Override
    public boolean isSortDirectionUp() {
        return sortDirectionUp;
    }

    @Override
    public void setSortDirectionUp(boolean isUp) {
        sortDirectionUp = isUp;
    }

    @Override
    public String getGroupingColumn() {
        return groupingColumn;
    }

    @Override
    public void setGroupingColumn(String columnName) {
        groupingColumn = columnName;
    }

    @Override
    public Set<String> getSelectedRuleNames() {
        return selectedRuleNames;
    }

    @Override
    public void setSelectedRuleNames(Set<String> ruleNames) {
        selectedRuleNames = ruleNames;
    }

    @Override
    public int getSelectedPropertyTab() {
        return selectedPropertyTab;
    }

    @Override
    public void setSelectedPropertyTab(int anIndex) {
        selectedPropertyTab = anIndex;
    }
}
