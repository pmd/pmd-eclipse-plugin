/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

import name.herlin.command.AbstractProcessableCommand;
import name.herlin.command.CommandException;
import name.herlin.command.CommandProcessor;
import name.herlin.command.Timer;
import name.herlin.command.UnsetInputPropertiesException;

/**
 * This is a particular processor for Eclipse in order to handle long running
 * commands.
 *
 * @author Philippe Herlin
 *
 */
public class JobCommandProcessor implements CommandProcessor {
    private static final Logger LOG = Logger.getLogger(JobCommandProcessor.class);
    private final Map<AbstractProcessableCommand, Job> jobs = Collections
            .synchronizedMap(new HashMap<AbstractProcessableCommand, Job>());

    private static ConcurrentLinkedQueue<Job> outstanding = new ConcurrentLinkedQueue<Job>();
    private static AtomicInteger count = new AtomicInteger();

    /**
     * @see name.herlin.command.CommandProcessor#processCommand(name.herlin.command.AbstractProcessableCommand)
     */
    public void processCommand(final AbstractProcessableCommand aCommand) throws CommandException {
        LOG.debug("Begining job command " + aCommand.getName());

        if (!aCommand.isReadyToExecute()) {
            throw new UnsetInputPropertiesException();
        }

        final Job job = new Job(aCommand.getName()) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    if (aCommand instanceof AbstractDefaultCommand) {
                        ((AbstractDefaultCommand) aCommand).setMonitor(monitor);
                    }
                    Timer timer = new Timer();
                    aCommand.execute();
                    timer.stop();
                    PMDPlugin.getDefault().logInformation(
                            "Command " + aCommand.getName() + " excecuted in " + timer.getDuration() + "ms");
                } catch (CommandException e) {
                    PMDPlugin.getDefault().logError("Error executing command " + aCommand.getName(), e);
                }

                synchronized (outstanding) {
                    count.decrementAndGet();
                    Job job = outstanding.poll();
                    if (job != null) {
                        job.schedule();
                    }
                }
                return Status.OK_STATUS;
            }
        };

        if (aCommand instanceof AbstractDefaultCommand) {
            job.setUser(((AbstractDefaultCommand) aCommand).isUserInitiated());
        }

        synchronized (outstanding) {
            if (count.incrementAndGet() > 10) {
                // too many already running, put in a queue to run later
                outstanding.add(job);
            } else {
                job.schedule();
            }
        }
        this.addJob(aCommand, job);
        LOG.debug("Ending job command " + aCommand.getName());
    }

    /**
     * @see name.herlin.command.CommandProcessor#waitCommandToFinish(name.herlin.command.AbstractProcessableCommand)
     */
    public void waitCommandToFinish(final AbstractProcessableCommand aCommand) throws CommandException {
        final Job job = this.jobs.get(aCommand);
        if (job != null) {
            try {
                job.join();
            } catch (InterruptedException e) {
                throw new CommandException(e);
            }
        }

    }

    /**
     * Add a job to the map. Also, clear all finished jobs
     * 
     * @param command
     *            for which to keep the job
     * @param job
     *            a job to keep until it is finished
     */
    private void addJob(final AbstractProcessableCommand command, final Job job) {
        this.jobs.put(command, job);

        // clear terminated command
        final Iterator<AbstractProcessableCommand> i = this.jobs.keySet().iterator();
        while (i.hasNext()) {
            final AbstractProcessableCommand aCommand = i.next();
            final Job aJob = this.jobs.get(aCommand);
            if (aJob == null || aJob.getResult() != null) {
                this.jobs.remove(aCommand);
            }
        }
    }
}
