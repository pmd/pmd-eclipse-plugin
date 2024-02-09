/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.dialogs;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.nls.StringTable;
import net.sourceforge.pmd.lang.rule.Rule;

public class ViolationDetailsDialogPage extends DialogPage {
    private final IMarker violation;
    private final Rule rule;

    public ViolationDetailsDialogPage(IMarker selectedViolation, Rule selectedRule) {
        this.violation = selectedViolation;
        this.rule = selectedRule;
    }

    @Override
    public void createControl(Composite parent) {
        StringTable messages = PMDPlugin.getDefault().getStringTable();

        GridLayout layout = new GridLayout(2, false);
        Composite composite = WidgetFactory.composite(SWT.NONE).layout(layout).create(parent);

        createLabel(messages.getString(StringKeys.DIALOG_VIOLATION_DETAILS_MESSAGE), composite);
        createText(getViolationMessage(), composite);

        createLabel(messages.getString(StringKeys.DIALOG_VIOLATION_DETAILS_LOCATION), composite);
        createText(getLocation(), composite);

        createLabel(messages.getString(StringKeys.DIALOG_VIOLATION_DETAILS_RULENAME), composite);
        createText(rule.getName(), composite);

        createLabel(messages.getString(StringKeys.DIALOG_VIOLATION_DETAILS_CATEGORY), composite);
        createText(rule.getRuleSetName(), composite);

        createLabel(messages.getString(StringKeys.DIALOG_VIOLATION_DETAILS_PRIORITY), composite);
        createText(rule.getPriority().getName(), composite);

        createTwoColumnLabel(messages.getString(StringKeys.DIALOG_VIOLATION_DETAILS_DESCRIPTION), composite);
        createTwoColumnMultiText(rule.getDescription(), composite);

        createTwoColumnLabel(messages.getString(StringKeys.DIALOG_VIOLATION_DETAILS_EXAMPLES), composite);
        createTwoColumnMultiText(getExamples(), JFaceResources.getTextFont(), composite);

        createLabel(messages.getString(StringKeys.DIALOG_VIOLATION_DETAILS_INFOURL), composite);
        createLink(rule.getExternalInfoUrl(), composite);
    }

    private Label createLabel(String text, Composite parent) {
        return WidgetFactory.label(SWT.NONE).text(text).font(JFaceResources.getHeaderFont())
            .layoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING)).create(parent);
    }

    private Label createTwoColumnLabel(String text, Composite parent) {
        GridData gridData = new GridData();
        gridData.verticalAlignment = SWT.BEGINNING;
        gridData.horizontalSpan = 2;
        return WidgetFactory.label(SWT.NONE).text(text).font(JFaceResources.getHeaderFont())
                .layoutData(gridData).create(parent);
    }
    
    private Text createText(String text, Composite parent) {
        return WidgetFactory.text(SWT.READ_ONLY | SWT.SINGLE)
            .background(parent.getBackground())
            .layoutData(new GridData(GridData.FILL_HORIZONTAL))
            .text(text)
            .create(parent);
    }

    private Text createTwoColumnMultiText(String text, Composite parent) {
        return createTwoColumnMultiText(text, null, parent);
    }

    private Text createTwoColumnMultiText(String text, Font font, Composite parent) {
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 2;
        return WidgetFactory.text(SWT.READ_ONLY | SWT.MULTI | SWT.WRAP)
            .background(parent.getBackground())
            .layoutData(gridData)
            .text(text)
            .font(font)
            .create(parent);
    }

    private Link createLink(String url, Composite parent) {
        Link link = new Link(parent, SWT.NONE);
        link.setText(String.format("<a href=\"%s\">%s</a>", url, url));
        link.addSelectionListener(new LinkClickListener());
        link.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        return link;
    }

    private String getViolationMessage() {
        String defaultMessage = violation.getAttribute(IMarker.MESSAGE, "");
        return violation.getAttribute(PMDRuntimeConstants.KEY_MARKERATT_MESSAGE, defaultMessage);
    }

    private String getLocation() {
        String projectName = violation.getResource().getProject().getName();
        String path = violation.getResource().getProjectRelativePath().toString();
        int lineNumber = violation.getAttribute(IMarker.LINE_NUMBER, -1);

        if (lineNumber != -1) {
            return String.format("%s: %s:%d", projectName, path, lineNumber);
        }
        return String.format("%s: %s", projectName, path);
    }

    private String getExamples() {
        StringBuilder result = new StringBuilder();
        for (String example : rule.getExamples()) {
            result.append(example);
            result.append(System.lineSeparator());
            result.append(System.lineSeparator());
        }
        return result.toString();
    }

    private static final class LinkClickListener implements SelectionListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                URL url = new URL(e.text);
                IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
                browser.openURL(url);
            } catch (MalformedURLException | PartInitException e1) {
                PMDPlugin.getDefault().logError(e1.getMessage(), e1);
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    }
}
