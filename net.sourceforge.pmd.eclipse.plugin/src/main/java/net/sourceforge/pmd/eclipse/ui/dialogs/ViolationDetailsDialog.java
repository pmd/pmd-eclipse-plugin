/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.dialogs;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.builder.MarkerUtil;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

public class ViolationDetailsDialog extends Dialog {
    private final IMarker violation;

    public ViolationDetailsDialog(Shell shell, IMarker selectedViolation) {
        super(shell);
        this.violation = selectedViolation;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(PMDPlugin.getDefault().getStringTable().getString(StringKeys.DIALOG_VIOLATION_DETAILS_TITLE));
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    private Rule getSelectedViolationRule() {
        return PMDPlugin.getDefault().getPreferencesManager().getRuleSet()
                .getRuleByName(MarkerUtil.ruleNameFor(violation));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        ScrolledComposite scroll = new ScrolledComposite((Composite) super.createDialogArea(parent),
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        scroll.setLayout(new GridLayout());
        scroll.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

        Composite composite = WidgetFactory.composite(SWT.NONE).layout(new GridLayout())
                .layoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL))
                .create(scroll);
        scroll.setContent(composite);

        ViolationDetailsDialogPage content = new ViolationDetailsDialogPage(violation, getSelectedViolationRule());
        content.createControl(composite);
        composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return scroll;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        okPressed();
    }
}
