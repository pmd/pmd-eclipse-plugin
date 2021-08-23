/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.runtime.properties.PropertiesException;

/**
 * Rebuild a project to force PMD to be run on that project.
 *
 * @author Philippe Herlin
 *
 */
public class BuildProjectCommand extends AbstractProjectCommand {
    private static final Logger LOG = LoggerFactory.getLogger(BuildProjectCommand.class);

    public BuildProjectCommand() {
        super("BuildProject", "Rebuild a project.");

        setReadOnly(false);
        setOutputProperties(false);
        setTerminated(false);
    }

    @Override
    public void execute() {
        try {
            LOG.debug("Build for Project {} triggered, setting needRebuild=false", project().getName());
            // set needRebuild=false before triggering the build, as the build might load the properties
            // in parallel still with needRebuild=true
            projectProperties().setNeedRebuild(false);

            project().build(IncrementalProjectBuilder.FULL_BUILD, this.getMonitor());
        } catch (CoreException | PropertiesException e) {
            throw new RuntimeException(e);
        } finally {
            this.setTerminated(true);
        }
    }
}
