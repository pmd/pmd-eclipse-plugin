/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.properties;

import org.eclipse.core.resources.IProject;

/**
 * This interface specifies factory method to create objects from the properties package
 * 
 * @author Herlin
 *
 */

public interface IPropertiesFactory {
    
    /**
     * Get a properties manager
     */
    IProjectPropertiesManager getProjectPropertiesManager();
    
    /**
     * Instantiate a new Project Properties information structure for a particular project and
     * a particular project properties manager
     */
    IProjectProperties newProjectProperties(IProject project, IProjectPropertiesManager projetcPropertiesManager);
    

}
