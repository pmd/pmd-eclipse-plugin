/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.preferences;

import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptor;
import net.sourceforge.pmd.lang.rule.RulePriority;
import net.sourceforge.pmd.lang.rule.RuleSet;

/**
 * This interface specifies the behaviour of the preferences manager.
 * 
 * @author Herlin
 *
 */

public interface IPreferencesManager {
    
    /**
     * Load preferences from the preferences store or return the one that
     * was previously loaded.
     * 
     * @return the preferences structure
     */
    IPreferences loadPreferences();
    
    /**
     * Reload preferences from the preferences store.
     * 
     * @return the preferences structure
     */
    IPreferences reloadPreferences();
    
    /**
     * Store a preferences structure into the preferences store
     * @param preferences structure of the preferences information
     */
    void storePreferences(IPreferences preferences);
    
    
    // The following operations are for a transition period only

    /**
     * Get the configured rule set
     */
    RuleSet getRuleSet();

    /**
     * Gets the default rule set. This means, all rules, that the Plugin is aware of.
     */
    RuleSet getDefaultRuleSet();

    /**
     * Set the rule set and store it in the preferences
     */
    void setRuleSet(RuleSet newRuleSet);
    
    /**
     * 
     * @param priority
     * @return
     */
    PriorityDescriptor defaultDescriptorFor(RulePriority priority);
}
