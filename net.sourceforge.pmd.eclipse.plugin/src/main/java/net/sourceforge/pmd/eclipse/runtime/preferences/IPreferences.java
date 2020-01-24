/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.preferences;

import java.util.Set;

import org.apache.log4j.Level;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptor;

/**
 * This interface models the PMD Plugin preferences
 * 
 * @author Herlin
 *
 */

public interface IPreferences {

    // General Preferences

    boolean PROJECT_BUILD_PATH_ENABLED_DEFAULT = true;
    boolean PMD_PERSPECTIVE_ENABLED_DEFAULT = true;
    boolean PMD_VIOLATIONS_OVERVIEW_ENABLED_DEFAULT = false;
    boolean PMD_VIOLATIONS_OUTLINE_ENABLED_DEFAULT = false;
    boolean PMD_CHECK_AFTER_SAVE_DEFAULT = false;
    boolean PMD_USE_CUSTOM_PRIORITY_NAMES_DEFAULT = true;
    int MAX_VIOLATIONS_PFPR_DEFAULT = 5;
    boolean DETERMINE_FILETYPES_AUTOMATICALLY_DEFAULT = true;
    String REVIEW_ADDITIONAL_COMMENT_DEFAULT = "by {0} on {1}";
    boolean REVIEW_PMD_STYLE_ENABLED_DEFAULT = true;
    int MIN_TILE_SIZE_DEFAULT = 25;
    String LOG_FILENAME_DEFAULT = System.getProperty("user.home") + "/pmd-eclipse.log";
    @Deprecated // use LOG_LEVEL_DEFAULT instead
    Level LOG_LEVEL = Level.WARN;
    String LOG_LEVEL_DEFAULT = "WARN";

    // default renderer
    String ACTIVE_RENDERERS = "text";
    String ACTIVE_EXCLUSIONS = "";
    String ACTIVE_INCLUSIONS = "";

    boolean GLOBAL_RULE_MANAGEMENT_DEFAULT = false;

    int TABLE_FRACTION_DEFAULT = 55;
    boolean DEFAULT_SORT_UP = false;
    String DEFAULT_GROUPING_COLUMN = "";

    /**
     * Get a comma-separated list of rules that are active by default.
     */
    String getDefaultActiveRules();

    boolean boolFor(String prefId);

    void boolFor(String prefId, boolean newValue);

    boolean isActive(String rulename);

    void isActive(String ruleName, boolean flag);

    boolean isActiveRenderer(String rendererName);

    boolean getGlobalRuleManagement();

    void setGlobalRuleManagement(boolean b);

    Set<String> getActiveRuleNames();

    void setActiveRuleNames(Set<String> ruleNames);

    /**
     * Should the Project Build Path be used?
     */
    boolean isProjectBuildPathEnabled();

    /**
     * Set whether using the Project Build Path?
     */
    void setProjectBuildPathEnabled(boolean projectBuildPathEnabled);

    /**
     * Should the plugin switch to the PMD perspective when a manual code review
     * is launched ?
     */
    boolean isPmdPerspectiveEnabled();
    
    /**
     * Should the plugin show the PMD violations overview when a code review is launched?
     * @return
     */
    boolean isPmdViolationsOverviewEnabled();
    
    /**
     * Should the plugin show the PMD violations outline when a code review is launched?
     * @return
     */
    boolean isPmdViolationsOutlineEnabled();

    /**
     * Should the plugin scan any newly-saved code?
     */
    boolean isCheckAfterSaveEnabled();

    boolean useCustomPriorityNames();

    void useCustomPriorityNames(boolean flag);

    /**
     * Should the plugin scan any newly-saved code?
     */
    void isCheckAfterSaveEnabled(boolean flag);

    /**
     * Set whether the plugin switch to the PMD perspective when a manual code
     * review is launched
     */
    void setPmdPerspectiveEnabled(boolean pmdPerspectiveEnabled);
    
    /**
     * Set whether the plugin switch to the PMD violations overview when a manual code
     * review is launched
     */
    void setPmdViolationsOverviewEnabled(boolean pmdViolationsOverviewEnabled);
    
    /**
     * Set whether the plugin switch to the PMD violations outline when a manual code
     * review is launched
     */
    void setPmdViolationsOutlineEnabled(boolean pmdViolationsOutlineEnabled);

    /**
     * Get the maximum number of violations per file per rule reported by the
     * plugin. This parameter is used to improve performances
     */
    int getMaxViolationsPerFilePerRule();

    /**
     * Set the maximum number of violations per file per rule reported by the
     * plugin
     * 
     * @param maxViolationPerFilePerRule
     */
    void setMaxViolationsPerFilePerRule(int maxViolationPerFilePerRule);

    /**
     * If true: When checking, whether a given file should be analyzed by PMD, take
     * the rule's language and the language's file extensions into account.
     * @return
     */
    boolean isDetermineFiletypesAutomatically();

    /**
     * Sets whether the rule's language file extensions should be considered or not.
     * @param determineFiletypesAutomatically
     */
    void setDetermineFiletypesAutomatically(boolean determineFiletypesAutomatically);

    /**
     * Get the review additional comment. This comment is a text appended to the
     * review comment that is inserted into the code when a violation is
     * reviewed. This string follows the MessageFormat syntax and could contain
     * 2 variable fields. The 1st field is replaced by the current used id and
     * the second by the current date.
     */
    String getReviewAdditionalComment();

    /**
     * Set the review additional comment.
     * 
     * @param reviewAdditionalComment
     */
    void setReviewAdditionalComment(String reviewAdditionalComment);

    /**
     * Does the review comment should be the PMD style (// NOPMD comment) or the
     * plugin style (// @PMD:REVIEW...) which was implemented before.
     */
    boolean isReviewPmdStyleEnabled();

    /**
     * Set whether the PMD review comment should be used instead of the plugin
     * comment.
     */
    void setReviewPmdStyleEnabled(boolean reviewPmdStyleEnabled);

    void setPriorityDescriptor(RulePriority priority, PriorityDescriptor pd);

    PriorityDescriptor getPriorityDescriptor(RulePriority priority);

    // CPD Preferences

    /**
     * Get the CPD minimum tile size, ie. the number of lines that could be
     * duplicated. ie. lower it is, more duplicated will be found.
     */
    int getMinTileSize();

    /**
     * Set the CPD minimum tile size
     */
    void setMinTileSize(int minTileSize);

    /**
     * Get the log filename
     */
    String getLogFileName();

    /**
     * Set the log filename
     */
    void setLogFileName(String logFileName);

    /**
     * Return the log level
     * @deprecated use {@link #getLogLevelName()}
     */
    @Deprecated
    Level getLogLevel();

    String getLogLevelName();

    /**
     * Set the log level
     * @deprecated use {@link #setLogLevel(String)}
     */
    @Deprecated
    void setLogLevel(Level level);

    void setLogLevel(String level);

    // Globally configured rules

    // later...

    /**
     * 
     */
    Set<String> activeReportRenderers();

    /**
     * 
     * @param names
     */
    void activeReportRenderers(Set<String> names);

    /**
     * 
     */
    Set<String> activeExclusionPatterns();

    /**
     * 
     * @param names
     */
    void activeExclusionPatterns(Set<String> filterPatterns);

    /**
     * 
     */
    Set<String> activeInclusionPatterns();

    /**
     * 
     * @param names
     */
    void activeInclusionPatterns(Set<String> filterPatterns);

    /**
     * Synchronize the preferences with the preferences store
     */
    void sync();

    int tableFraction();

    void tableFraction(int aFraction);

    Set<String> getHiddenColumnIds();

    void setHiddenColumnIds(Set<String> names);

    boolean isSortDirectionUp();

    void setSortDirectionUp(boolean isUp);

    String getGroupingColumn();

    void setGroupingColumn(String columnName);

    Set<String> getSelectedRuleNames();

    void setSelectedRuleNames(Set<String> ruleNames);

    int getSelectedPropertyTab();

    void setSelectedPropertyTab(int anIndex);

}
