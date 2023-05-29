/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPropertyListener;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;
import net.sourceforge.pmd.eclipse.util.internal.IOUtil;
import net.sourceforge.pmd.lang.document.FileId;

/**
 * This command reviews a resource - a file - for a specific rule.
 *
 * <p>Used for the DFA table.
 *
 * @author Sven
 *
 */
public class ReviewResourceForRuleCommand extends AbstractDefaultCommand {

    private IResource resource;
    private Rule rule;
    private List<IPropertyListener> listenerList;

    public ReviewResourceForRuleCommand() {
        super("ReviewResourceForRuleCommand", "Review a resource for a specific rule.");

        setOutputProperties(true);
        setReadOnly(true);
        setTerminated(false);
        listenerList = new ArrayList<>();
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

    @Override
    public void reset() {
        setResource(null);
        setRule(null);
        listenerList = new ArrayList<>();
    }

    @Override
    public void execute() {
        IFile file = (IFile) resource.getAdapter(IFile.class);
        beginTask("PMD checking for rule: " + rule.getName(), 1);

        if (file != null) {
            RuleSet ruleSet = RuleSetUtil.newSingle(rule);

            File sourceCodeFile = file.getFullPath().toFile();
            FileId fileId = FileId.fromPathLikeString(sourceCodeFile.toString());
            if (ruleSet.applies(fileId)) {
                PMDConfiguration configuration = new PMDConfiguration();
                Report report = null;

                try (Reader input = new InputStreamReader(file.getContents(), file.getCharset());
                     PmdAnalysis pmdAnalysis = PmdAnalysis.create(configuration)) {

                    pmdAnalysis.addRuleSet(ruleSet);
                    pmdAnalysis.files().addSourceFile(fileId, IOUtil.toString(input));

                    report = pmdAnalysis.performAnalysisAndCollectReport();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                final Report finalResult = report;
                // trigger event propertyChanged for all listeners
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        for (IPropertyListener listener : listenerList) {
                            listener.propertyChanged(finalResult.getViolations().iterator(),
                                    PMDRuntimeConstants.PROPERTY_REVIEW);
                        }
                    }
                });
            }
        }
    }
}
