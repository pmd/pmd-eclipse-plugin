/**
 * 
 */
package net.sourceforge.pmd.eclipse.runtime.preferences.impl;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferencesManager;

import org.junit.Assert;
import org.junit.Test;

public class PreferencesManagerImplTest {

    /**
     * See bug https://sourceforge.net/p/pmd/bugs/1184/
     *
     * Inactive rules were not stored / loaded
     */
    @Test
    public void storeAndLoadInactiveRules() {
        String ruleName = "LocalVariableCouldBeFinal";

        IPreferencesManager manager = PMDPlugin.getDefault().getPreferencesManager();
        IPreferences preferences = manager.loadPreferences();
        preferences.isActive(ruleName, false);
        manager.storePreferences(preferences);
        Assert.assertFalse(preferences.getInactiveRuleNames().isEmpty());
        Assert.assertTrue(preferences.getInactiveRuleNames().contains(ruleName));

        preferences = manager.reloadPreferences();
        Assert.assertFalse(preferences.getInactiveRuleNames().isEmpty());
        Assert.assertTrue(preferences.getInactiveRuleNames().contains(ruleName));
    }
}
