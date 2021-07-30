/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.actions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Shell;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.builder.MarkerUtil;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.preferences.RuleDialog;

/**
 * Implements the call of the rule dialog to show rule data.
 *
 * @author Philippe Herlin
 *
 */
public class ShowRuleAction extends AbstractViolationSelectionAction {

    private Shell shell;

    public ShowRuleAction(TableViewer viewer, Shell shell) { // NOPMD: unused formal parameter shell TODO
        super(viewer);
    }

    @Override
    protected String textId() {
        return StringKeys.VIEW_ACTION_SHOW_RULE;
    }

    @Override
    protected String imageId() {
        return null;
    }

    @Override
    protected String tooltipMsgId() {
        return StringKeys.VIEW_TOOLTIP_SHOW_RULE;
    }

    @Override
    protected boolean canExecute() {
        return super.canExecute() && allSelectionsDenoteSameRule();
    }

    private boolean allSelectionsDenoteSameRule() {
        IMarker[] markers = getSelectedViolations();
        return MarkerUtil.commonRuleNameAmong(markers) != null;
    }

    @Override
    public void run() {
        Rule selectedRule = getSelectedViolationRule();
        if (selectedRule != null) {
            RuleDialog ruleDialog = new RuleDialog(shell, selectedRule, false);
            ruleDialog.open();
        }
    }

    /**
     * Returns the rule from the first selected violation.
     */
    public Rule getSelectedViolationRule() {
        Rule rule = null;
        try {
            IMarker[] markers = getSelectedViolations();
            if (markers != null) {
                rule = PMDPlugin.getDefault().getPreferencesManager().getRuleSet()
                        .getRuleByName(MarkerUtil.ruleNameFor(markers[0]));
            }
        } catch (RuntimeException e) {
            logErrorByKey(StringKeys.ERROR_RUNTIME_EXCEPTION, e);
        }

        return rule;
    }
}
