/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.properties.impl;

import org.eclipse.core.resources.IProject;

import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectPropertiesManager;
import net.sourceforge.pmd.eclipse.runtime.properties.IPropertiesFactory;

/**
 * Implements a factory for the objects of the properties package.
 * 
 * @author Herlin
 */
public class PropertiesFactoryImpl implements IPropertiesFactory {
    private IProjectPropertiesManager projectPropertiesManager;

    @Override
    public IProjectPropertiesManager getProjectPropertiesManager() {
        if (this.projectPropertiesManager == null) {
            this.projectPropertiesManager = new ProjectPropertiesManagerImpl();
        }
        
        return this.projectPropertiesManager;
    }

    @Override
    public IProjectProperties newProjectProperties(IProject project, IProjectPropertiesManager newProjectPropertiesManager) {
        return new ProjectPropertiesImpl(project, newProjectPropertiesManager);
    }

}
