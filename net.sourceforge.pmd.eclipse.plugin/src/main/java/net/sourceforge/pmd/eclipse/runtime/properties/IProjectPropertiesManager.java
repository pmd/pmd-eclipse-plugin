/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.properties;

import org.eclipse.core.resources.IProject;

/**
 * Project Properties Manager interface.
 * 
 * @author Philippe Herlin
 *
 */
public interface IProjectPropertiesManager {
    /**
     * Load a project properties
     * 
     * @param project
     *            a project
     */
    IProjectProperties loadProjectProperties(IProject project) throws PropertiesException;

    /**
     * Save project properties
     * 
     * @param projectProperties
     *            the project properties to save
     * @throws PropertiesException
     */
    void storeProjectProperties(IProjectProperties projectProperties) throws PropertiesException;

    /**
     * Remove a project when it is deleted.
     * 
     * @param project
     *            the project
     */
    void removeProjectProperties(IProject project);
}
