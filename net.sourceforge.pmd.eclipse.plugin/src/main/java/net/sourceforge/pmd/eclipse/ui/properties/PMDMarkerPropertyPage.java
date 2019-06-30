/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.properties;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.dialogs.PropertyPage;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.builder.MarkerUtil;
import net.sourceforge.pmd.eclipse.ui.nls.StringTable;

public class PMDMarkerPropertyPage extends PropertyPage {

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);


        IMarker marker = (IMarker) getElement();
        Rule rule = PMDPlugin.getDefault().getPreferencesManager().getRuleSet()
                .getRuleByName(MarkerUtil.ruleNameFor(marker));

        StringTable messages = PMDPlugin.getDefault().getStringTable();

        try {
            addLabel(composite, messages.getString("markerPropertyPage.label.rulename"));
            addText(composite, rule.getName());

            addLabel(composite, messages.getString("markerPropertyPage.label.category"));
            addText(composite, rule.getRuleSetName());

            addLabel(composite, messages.getString("markerPropertyPage.label.priority"));
            addText(composite, rule.getPriority().name());

            addLabel(composite, messages.getString("markerPropertyPage.label.message"));
            addText(composite, getViolationMessage(marker));

            addLabel(composite, messages.getString("markerPropertyPage.label.description"), 2);
            addDescription(composite, rule);

            addLabel(composite, messages.getString("markerPropertyPage.label.externalInfoUrl"));
            addLink(composite, rule);
        } catch (CoreException e) {
            PMDPlugin.getDefault().logError(e.getMessage(), e);
        }

        return composite;
    }

    private String getViolationMessage(IMarker marker) throws CoreException {
        String defaultMessage = marker.getAttribute(IMarker.MESSAGE, "");
        return marker.getAttribute(PMDRuntimeConstants.KEY_MARKERATT_MESSAGE, defaultMessage);
    }

    private void addDescription(Composite composite, Rule rule) {
        Text descriptionText = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 2;
        gridData.heightHint = 50;
        descriptionText.setLayoutData(gridData);
        descriptionText.setText(rule.getDescription());
    }

    private void addLink(Composite composite, Rule rule) {
        Link link = new Link(composite, SWT.NONE);
        link.setText("<a href=\"" + rule.getExternalInfoUrl() + "\">" + rule.getExternalInfoUrl() + "</a>");
        link.addSelectionListener(new LinkClickListener());
    }

    private void addText(Composite parent, String value) {
        Text text = new Text(parent, SWT.READ_ONLY | SWT.SINGLE);
        text.setBackground(parent.getBackground());
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        text.setLayoutData(gridData);
        text.setText(value);
    }

    private void addLabel(Composite parent, String label) {
        addLabel(parent, label, 1);
    }

    private void addLabel(Composite parent, String text, int columnSpan) {
        Label label = new Label(parent, SWT.NONE);
        if (columnSpan > 1) {
            GridData gridData = new GridData();
            gridData.horizontalSpan = columnSpan;
            label.setLayoutData(gridData);
        }
        label.setText(text);
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
