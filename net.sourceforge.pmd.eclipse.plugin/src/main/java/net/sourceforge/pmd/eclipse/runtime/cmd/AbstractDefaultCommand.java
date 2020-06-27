/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.lang.Language;

/**
 * This is a base implementation for a command inside the PMD plugin. This must
 * be used as a root implementation for all the plugin commands.
 *
 * @author Philippe Herlin
 *
 */
public abstract class AbstractDefaultCommand {

    private boolean readOnly;
    private boolean outputProperties;
    private boolean readyToExecute;
    private final String description;
    private final String name;
    private IProgressMonitor monitor;
    private int stepCount;
    private boolean userInitiated;
    private boolean terminated;

    protected AbstractDefaultCommand(String theName, String theDescription) {
        name = theName;
        description = theDescription;
    }

    // private static final Logger log =
    // Logger.getLogger(AbstractDefaultCommand.class);

    public static void logInfo(String message) {
        PMDPlugin.getDefault().logInformation(message);
    }

    public static void logError(String message, Throwable error) {
        PMDPlugin.getDefault().logError(message, error);
    }

    /**
     * 
     * @param file
     * @return
     * @deprecated we support multiple languages now
     */
    public static boolean isJavaFile(IFile file) {
        if (file == null) {
            return false;
        }
        return "JAVA".equalsIgnoreCase(file.getFileExtension());
    }

    public static boolean isLanguageFile(IFile file, Language language) {
        if (file == null) {
            return false;
        }
        return language.hasExtension(file.getFileExtension());
    }

    /**
     * @return Returns the readOnly status.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * @param readOnly
     *            The readOnly to set.
     */
    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param outputProperties
     *            The outputProperties to set.
     */
    public void setOutputProperties(final boolean outputProperties) {
        this.outputProperties = outputProperties;
    }

    /**
     * @return Returns the outputProperties.
     */
    public boolean hasOutputProperties() {
        return outputProperties;
    }

    /**
     * @return Returns the readyToExecute.
     */
    public boolean isReadyToExecute() {
        return readyToExecute;
    }

    /**
     * @param readyToExecute
     *            The readyToExecute to set.
     */
    public void setReadyToExecute(final boolean readyToExecute) {
        this.readyToExecute = readyToExecute;
    }

    /**
     * @return Returns the number of steps for that command
     */
    public int getStepCount() {
        return stepCount;
    }

    /**
     * @param stepsCount
     *            The number of steps for that command
     */
    public void setStepCount(final int stepCount) {
        this.stepCount = stepCount;
    }

    /**
     * @return Returns the userInitiated.
     */
    public boolean isUserInitiated() {
        return userInitiated;
    }

    /**
     * @param userInitiated
     *            The userInitiated to set.
     */
    public void setUserInitiated(boolean userInitiated) {
        this.userInitiated = userInitiated;
    }

    /**
     * @return Returns the monitor.
     */
    public IProgressMonitor getMonitor() {
        return monitor;
    }

    /**
     * @param monitor
     *            The monitor to set.
     */
    public void setMonitor(final IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    public abstract void execute();

    public abstract void reset();

    /**
     * delegate method for monitor.beginTask
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#beginTask
     */
    protected void beginTask(String name, int totalWork) {
        if (monitor != null) {
            monitor.beginTask(name, totalWork);
        }
    }

    /**
     * delegate method to monitor.done()
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#DONE
     */
    protected void done() {
        if (monitor != null) {
            monitor.done();
        }

        setTerminated(true);
    }

    protected void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    protected boolean isTerminated() {
        return terminated;
    }

    /**
     * delegate method for monitor.isCanceled()
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled
     */
    protected boolean isCanceled() {
        return monitor != null && monitor.isCanceled();
    }

    /**
     * delegate method for monitor.setTaskName()
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName
     */
    protected void setTaskName(String name) {
        if (monitor != null) {
            monitor.setTaskName(name);
        }
    }

    /**
     * delegate method for monitor.subTask()
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#subTask
     */
    protected void subTask(String name) {
        if (monitor != null) {
            monitor.subTask(name);
        }
    }

    /**
     * delegate method for monitor.worked()
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled
     */
    protected void worked(int work) {
        if (monitor != null) {
            monitor.worked(work);
        }
    }

    public final void performExecute() {
        JobCommandProcessor.getInstance().processCommand(this);
    }

    public final void join() {
        JobCommandProcessor.getInstance().waitCommandToFinish(this);
    }

    // /**
    // * Return a PMD Engine for that project. The engine is parameterized
    // * according to the target JDK of that project.
    // *
    // * @param project
    // * @return
    // */
    // protected PMDEngine getPmdEngineForProject(IProject project) throws
    // CommandException {
    // IJavaProject javaProject = JavaCore.create(project);
    // PMDEngine pmdEngine = new PMDEngine();
    //
    // if (javaProject.exists()) {
    // String compilerCompliance =
    // javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
    // log.debug("compilerCompliance = " + compilerCompliance);
    //
    // LanguageVersion languageVersion =
    // Language.JAVA.getVersion(compilerCompliance);
    // if ( languageVersion == null ) {
    // throw new CommandException("The target JDK, " + compilerCompliance + " is
    // not supported"); // TODO NLS
    // }
    // pmdEngine.setLanguageVersion(languageVersion);
    //
    // IPreferences preferences = PMDPlugin.getDefault().loadPreferences();
    // if (preferences.isProjectBuildPathEnabled()) {
    // pmdEngine.setClassLoader(new
    // JavaProjectClassLoader(pmdEngine.getClassLoader(), javaProject));
    // }
    // } else {
    // throw new CommandException("The project " + project.getName() + " is not
    // a Java project"); // TODO NLS
    // }
    // return pmdEngine;
    // }
}
