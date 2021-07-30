/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;
import net.sourceforge.pmd.eclipse.runtime.properties.PropertiesException;

/**
 * 
 * @author Brian Remedios
 */
public abstract class AbstractProjectCommand extends AbstractDefaultCommand {

    private IProject project;

    protected AbstractProjectCommand(String theName, String theDescription) {
        super(theName, theDescription);
    }

    @Override
    public void reset() {
        setProject(null);
        setTerminated(false);
    }

    public void setProject(final IProject theProject) {
        project = theProject;
        setReadyToExecute(project != null);
    }

    protected IProject project() {
        return project;
    }

    protected void visitProjectResourcesWith(IResourceVisitor visitor) throws CoreException {
        project.accept(visitor);
    }

    @Override
    public boolean isReadyToExecute() {
        return project != null;
    }

    protected IFolder getProjectFolder(String folderId) {
        return project.getFolder(folderId);
    }

    protected IProjectProperties projectProperties() throws PropertiesException {
        return PMDPlugin.getDefault().loadProjectProperties(project);
    }
}
