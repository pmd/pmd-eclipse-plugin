/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPropertyListener;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PMDException;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.SourceCodeProcessor;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;
import net.sourceforge.pmd.eclipse.ui.actions.internal.InternalRuleSetUtil;

/**
 * This command reviews a resource - a file - for a specific rule.
 *
 * @author Sven
 *
 */
public class ReviewResourceForRuleCommand extends AbstractDefaultCommand {

    private IResource resource;
    private RuleContext context;
    private Rule rule;
    private List<IPropertyListener> listenerList;

    public ReviewResourceForRuleCommand() {
        super("ReviewResourceForRuleCommand", "Review a resource for a specific rule.");

        setOutputProperties(true);
        setReadOnly(true);
        setTerminated(false);
        listenerList = new ArrayList<IPropertyListener>();
    }

    public void setResource(IResource resource) {
        this.resource = resource;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    /**
     * Adds an object that wants to get an event after the command is finished.
     * 
     * @param listener
     *            the property listener to set.
     */
    public void addPropertyListener(IPropertyListener listener) {
        listenerList.add(listener);
    }

    @Override
    public boolean isReadyToExecute() {
        return resource != null && rule != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.sourceforge.pmd.eclipse.runtime.cmd.AbstractDefaultCommand#reset()
     */
    @Override
    public void reset() {
        setResource(null);
        setRule(null);
        listenerList = new ArrayList<IPropertyListener>();
    }

    @Override
    public void execute() {
        // IProject project = resource.getProject();
        IFile file = (IFile) resource.getAdapter(IFile.class);
        beginTask("PMD checking for rule: " + rule.getName(), 1);

        if (file != null) {
            RuleSet ruleSet = RuleSetUtil.newSingle(rule);
            // final PMDEngine pmdEngine = getPmdEngineForProject(project);
            File sourceCodeFile = file.getFullPath().toFile();
            if (ruleSet.applies(sourceCodeFile)) {
                try {
                    context = PMD.newRuleContext(file.getName(), sourceCodeFile);

                    // Reader input = new InputStreamReader(file.getContents(),
                    // file.getCharset());
                    new SourceCodeProcessor(new PMDConfiguration()).processSourceCode(file.getContents(),
                            InternalRuleSetUtil.toRuleSets(Collections.singletonList(ruleSet)),
                            context);
                    // input.close();
                    // } catch (CoreException e) {
                    // throw new CommandException(e);
                } catch (PMDException | CoreException e) {
                    throw new RuntimeException(e);
                }

                // trigger event propertyChanged for all listeners
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        for (IPropertyListener listener : listenerList) {
                            listener.propertyChanged(context.getReport().getViolations().iterator(),
                                    PMDRuntimeConstants.PROPERTY_REVIEW);
                        }
                    }
                });
            }
        }
    }
}
