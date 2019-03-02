/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.preferences.impl;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferencesManager;

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
        Assert.assertFalse(preferences.getActiveRuleNames().contains(ruleName));

        preferences = manager.reloadPreferences();
        Assert.assertFalse(preferences.getActiveRuleNames().contains(ruleName));
    }
}
