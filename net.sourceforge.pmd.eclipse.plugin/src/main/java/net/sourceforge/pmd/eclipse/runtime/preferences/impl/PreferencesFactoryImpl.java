/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.preferences.impl;

import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferencesFactory;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferencesManager;

/**
 * This class is an implementation for the factory of the Preferences package objects.
 * 
 * @author Herlin
 *
 */

public class PreferencesFactoryImpl implements IPreferencesFactory {
    private IPreferencesManager preferencesManager = null;

    @Override
    public IPreferencesManager getPreferencesManager() {
        if (preferencesManager == null) {
            preferencesManager = new PreferencesManagerImpl();
        }
        
        return preferencesManager;
    }

    @Override
    public IPreferences newPreferences(IPreferencesManager thePreferencesManager) {
        return new PreferencesImpl(thePreferencesManager);
    }

}
