/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

import net.sourceforge.pmd.eclipse.runtime.properties.PropertiesException;

import name.herlin.command.CommandException;

/**
 * Rebuild a project to force PMD to be run on that project.
 *
 * @author Philippe Herlin
 *
 */
public class BuildProjectCommand extends AbstractProjectCommand {

    private static final long serialVersionUID = 1L;

    /**
     * @param NAME
     */
    public BuildProjectCommand() {
        super("BuildProject", "Rebuild a project.");

        setReadOnly(false);
        setOutputProperties(false);
        setTerminated(false);
    }

    /**
     * @see name.herlin.command.AbstractProcessableCommand#execute()
     */
    public void execute() throws CommandException {
        try {
            project().build(IncrementalProjectBuilder.FULL_BUILD, this.getMonitor());

            projectProperties().setNeedRebuild(false);
        } catch (CoreException e) {
            throw new CommandException(e);
        } catch (PropertiesException e) {
            throw new CommandException(e);
        } finally {
            this.setTerminated(true);
        }
    }
}
