/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.preferences;

/**
 * Factory for all the preferences package objects
 * 
 * @author Herlin
 *
 */

public interface IPreferencesFactory {
    
    /**
     * @return the instance of the preferences manager
     */
    IPreferencesManager getPreferencesManager();
    
    /**
     * Return a new instance, not initialized, of a preferences information structure for the
     * specified preferences manager. 
     * @param prefencesManager a instance of a preferences manager
     */
    IPreferences newPreferences(IPreferencesManager prefencesManager);

}
