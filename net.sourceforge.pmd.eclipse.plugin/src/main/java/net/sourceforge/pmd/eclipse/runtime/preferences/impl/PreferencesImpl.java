/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.preferences.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.pmd.eclipse.core.IRuleSetManager;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferencesManager;
import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptor;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.lang.rule.RulePriority;
import net.sourceforge.pmd.lang.rule.RuleSet;

/**
 * Implements the preferences information structure.
 * 
 * @author Herlin
 *
 */

class PreferencesImpl implements IPreferences {

    private Map<String, Boolean> booleansById = new HashMap<>();

    private IPreferencesManager preferencesManager;
    private boolean projectBuildPathEnabled;
    private boolean pmdPerspectiveEnabled;
    private boolean pmdViolationsOverviewEnabled;
    private boolean pmdViolationsOutlineEnabled;
    private boolean checkAfterSaveEnabled;
    private boolean useCustomPriorityNames;
    private boolean determineFiletypesAutomatically;
    private String reviewAdditionalComment;
    private boolean reviewPmdStyleEnabled;
    private int minTileSize;
    private String logFileName;
    private String logLevel;
    private boolean globalRuleManagement;
    private Set<String> activeRuleNames = new HashSet<>();
    private Set<String> activeRendererNames = new HashSet<>();
    private Set<String> activeExclusionPatterns = new HashSet<>();
    private Set<String> activeInclusionPatterns = new HashSet<>();

    private Map<RulePriority, PriorityDescriptor> uiDescriptorsByPriority = new HashMap<>(5);

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

    @Override
    public boolean boolFor(String prefId) {
        Boolean value = booleansById.get(prefId);
        if (value == null) {
            throw new IllegalArgumentException("Unknown pref id: " + prefId);
        }
        return value;
    }

    @Override
    public void boolFor(String prefId, boolean newValue) {
        booleansById.put(prefId, newValue);
    }

    @Override
    public boolean isProjectBuildPathEnabled() {
        return projectBuildPathEnabled;
    }

    @Override
    public void setProjectBuildPathEnabled(boolean theProjectBuildPathEnabled) {
        this.projectBuildPathEnabled = theProjectBuildPathEnabled;
    }

    @Override
    public boolean isPmdPerspectiveEnabled() {
        return pmdPerspectiveEnabled;
    }

    @Override
    public boolean isCheckAfterSaveEnabled() {
        return checkAfterSaveEnabled;
    }

    @Override
    public void isCheckAfterSaveEnabled(boolean flag) {
        checkAfterSaveEnabled = flag;
    }

    @Override
    public void setPmdPerspectiveEnabled(boolean thePmdPerspectiveEnabled) {
        this.pmdPerspectiveEnabled = thePmdPerspectiveEnabled;
    }

    @Override
    public void setPmdViolationsOverviewEnabled(boolean thePmdViolationsOverviewEnabled) {
        this.pmdViolationsOverviewEnabled = thePmdViolationsOverviewEnabled;
    }

    @Override
    public boolean isDetermineFiletypesAutomatically() {
        return determineFiletypesAutomatically;
    }

    @Override
    public void setDetermineFiletypesAutomatically(boolean newDetermineFiletypesAutomatically) {
        this.determineFiletypesAutomatically = newDetermineFiletypesAutomatically;
    }

    @Override
    public String getReviewAdditionalComment() {
        return reviewAdditionalComment;
    }

    @Override
    public void setReviewAdditionalComment(String newReviewAdditionalComment) {
        this.reviewAdditionalComment = newReviewAdditionalComment;
    }

    @Override
    public boolean isReviewPmdStyleEnabled() {
        return reviewPmdStyleEnabled;
    }

    @Override
    public void setReviewPmdStyleEnabled(boolean newReviewPmdStyleEnabled) {
        this.reviewPmdStyleEnabled = newReviewPmdStyleEnabled;
    }

    @Override
    public int getMinTileSize() {
        return minTileSize;
    }

    @Override
    public void setMinTileSize(int newMinTileSize) {
        this.minTileSize = newMinTileSize;
    }

    @Override
    public String getLogFileName() {
        return logFileName;
    }

    @Override
    public String getLogLevelName() {
        return logLevel;
    }

    @Override
    public void setLogFileName(String theLogFileName) {
        this.logFileName = theLogFileName;
    }

    @Override
    public void setLogLevel(String level) {
        logLevel = level;
    }

    @Override
    public void sync() {
        preferencesManager.storePreferences(this);
    }

    @Override
    public boolean getGlobalRuleManagement() {
        return globalRuleManagement;
    }

    @Override
    public void setGlobalRuleManagement(boolean b) {
        globalRuleManagement = b;
    }

    @Override
    public boolean isActive(String ruleName) {
        return activeRuleNames.contains(ruleName);
    }

    @Override
    public boolean isActiveRenderer(String rendererName) {
        return activeRendererNames.contains(rendererName);
    }

    @Override
    public void isActive(String ruleName, boolean isActive) {
        if (isActive) {
            activeRuleNames.add(ruleName);
        } else {
            activeRuleNames.remove(ruleName);
        }
    }

    @Override
    public Set<String> getActiveRuleNames() {
        return activeRuleNames;
    }

    /**
     * Returns a comma-separated list of rules that are active by default. This
     * contains all default rules of PMD.
     */
    @Override
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

    @Override
    public void setActiveRuleNames(Set<String> ruleNames) {
        activeRuleNames = ruleNames;
    }

    @Override
    public Set<String> activeExclusionPatterns() {
        return activeExclusionPatterns;
    }

    @Override
    public void activeExclusionPatterns(Set<String> patterns) {
        activeExclusionPatterns = patterns;
    }

    @Override
    public Set<String> activeInclusionPatterns() {
        return activeInclusionPatterns;
    }

    @Override
    public void activeInclusionPatterns(Set<String> patterns) {
        activeInclusionPatterns = patterns;
    }

    @Override
    public void setPriorityDescriptor(RulePriority priority, PriorityDescriptor pd) {
        uiDescriptorsByPriority.put(priority, pd);
    }

    @Override
    public PriorityDescriptor getPriorityDescriptor(RulePriority priority) {
        return uiDescriptorsByPriority.get(priority);
    }

    @Override
    public boolean useCustomPriorityNames() {
        return useCustomPriorityNames;
    }

    @Override
    public void useCustomPriorityNames(boolean flag) {
        useCustomPriorityNames = flag;
    }

    @Override
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
