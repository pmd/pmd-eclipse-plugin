/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.panelmanagers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.editors.SWTUtil;
import net.sourceforge.pmd.lang.rule.RuleReference;

/**
 *
 * @author Brian Remedios
 */
public class DescriptionPanelManager extends AbstractRulePanelManager {

    private Text descriptionBox;
    private Text externalURLField;
    private Label extURLLabel;
    private Button browseButton;
    private Label messageLabel;
    private Text messageField;

    public static final String ID = "description";

    public DescriptionPanelManager(String theTitle, EditorUsageMode theMode, ValueChangeListener theListener) {
        super(ID, theTitle, theMode, theListener);
    }

    @Override
    protected boolean canManageMultipleRules() {
        return false;
    }

    @Override
    protected void clearControls() {
        descriptionBox.setText("");
        externalURLField.setText("");
    }

    @Override
    public void showControls(boolean flag) {
        descriptionBox.setVisible(flag);
        externalURLField.setVisible(flag);
        browseButton.setVisible(flag);
        extURLLabel.setVisible(flag);
        messageLabel.setVisible(flag);
        messageField.setVisible(flag);
    }

    @Override
    protected void updateOverridenFields() {
        Rule rule = soleRule();

        if (rule instanceof RuleReference) {
            RuleReference ruleReference = (RuleReference) rule;
            messageField.setBackground(ruleReference.getOverriddenMessage() != null ? overridenColour : null);
            descriptionBox.setBackground(ruleReference.getOverriddenDescription() != null ? overridenColour : null);
            externalURLField
                    .setBackground(ruleReference.getOverriddenExternalInfoUrl() != null ? overridenColour : null);
        }
    }

    @Override
    public Control setupOn(Composite parent) {
        initializeOn(parent);

        GridData gridData;

        Composite panel = new Composite(parent, 0);
        GridLayout layout = new GridLayout(3, false);
        panel.setLayout(layout);

        descriptionBox = newTextField(panel);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 3;
        descriptionBox.setLayoutData(gridData);

        descriptionBox.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                Rule soleRule = soleRule();
                if (soleRule == null) {
                    return;
                }

                String cleanValue = asCleanString(descriptionBox.getText());
                String existingValue = soleRule.getDescription();

                if (StringUtils.equals(StringUtils.stripToNull(existingValue), StringUtils.stripToNull(cleanValue))) {
                    return;
                }

                soleRule.setDescription(cleanValue);
                valueChanged(null, cleanValue);
                validateRuleParams(); // TODO hang off of valueChanged instead?
            }
        });

        buildExternalUrlPanel(panel, SWTUtil.stringFor(StringKeys.PREF_RULEEDIT_LABEL_EXTERNAL_INFO_URL));
        buildMessagePanel(panel, SWTUtil.stringFor(StringKeys.PREF_RULEEDIT_LABEL_MESSAGE));

        return panel;
    }

    private void validateRuleParams() {
        validate();

        boolean urlOK = StringUtils.isBlank(externalURLField.getText()) || hasValidURL();
        adjustBrowseButton(urlOK);
    }

    private void buildExternalUrlPanel(Composite parent, String urlLabel) {
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);

        extURLLabel = new Label(parent, 0);
        extURLLabel.setText(urlLabel);
        gridData.horizontalSpan = 1;
        gridData.grabExcessHorizontalSpace = false;
        extURLLabel.setLayoutData(gridData);

        externalURLField = new Text(parent, SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 1;
        gridData.grabExcessHorizontalSpace = true;
        externalURLField.setLayoutData(gridData);

        browseButton = buildExternalInfoUrlButton(parent);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        gridData.horizontalSpan = 1;
        browseButton.setLayoutData(gridData);

        externalURLField.addListener(SWT.FocusOut, new Listener() {
            @Override
            public void handleEvent(Event e) {
                handleExternalURLChange();
            }
        });
        externalURLField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validateRuleParams();
            }
        });
    }

    private void buildMessagePanel(Composite parent, String messageLbl) {
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);

        messageLabel = new Label(parent, 0);
        messageLabel.setText(messageLbl);
        gridData.horizontalSpan = 1;
        gridData.grabExcessHorizontalSpace = false;
        messageLabel.setLayoutData(gridData);

        messageField = new Text(parent, SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        messageField.setLayoutData(gridData);

        messageField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                handleMessageChange();
                validateRuleParams();
            }
        });
    }

    private void handleExternalURLChange() {
        String newURL = asCleanString(externalURLField.getText());
        Rule rule = soleRule();

        if (!StringUtils.equals(StringUtils.stripToNull(asCleanString(rule.getExternalInfoUrl())),
                StringUtils.stripToNull(newURL))) {
            rule.setExternalInfoUrl(newURL);
            valueChanged(null, newURL);
        }

        adjustBrowseButton(hasValidURL());
    }

    private void handleMessageChange() {
        String newMessage = asCleanString(messageField.getText());
        Rule rule = soleRule();

        if (!StringUtils.equals(StringUtils.stripToNull(asCleanString(rule.getMessage())),
                StringUtils.stripToNull(newMessage))) {
            rule.setMessage(newMessage);
            updateUI();
        }
    }

    private Button buildExternalInfoUrlButton(Composite parent) {
        final Button button = new Button(parent, SWT.PUSH);
        button.setText(SWTUtil.stringFor(StringKeys.PREF_RULEEDIT_BUTTON_OPEN_EXTERNAL_INFO_URL));

        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String url = externalURLField.getText();
                if (url.length() > 0) {
                    try {
                        IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
                        browser.openURL(new URL(url));
                    } catch (PartInitException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return button;
    }

    @Override
    protected void adapt() {
        Rule soleRule = soleRule();

        if (soleRule == null) {
            shutdown(descriptionBox);
            shutdown(externalURLField);
            shutdown(messageField);
        } else {
            show(descriptionBox, asCleanString(soleRule.getDescription()));
            show(externalURLField, asCleanString(soleRule.getExternalInfoUrl()));
            show(messageField, asCleanString(soleRule.getMessage()));
        }

        boolean isValid = hasValidURL();
        adjustBrowseButton(isValid);
    }

    public static boolean isValidURL(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }

        String urlUC = url.toUpperCase(Locale.ROOT);
        if (!urlUC.startsWith("HTTP")) {
            return false;
        }

        for (int i = 0; i < url.length(); i++) {
            if (Character.isWhitespace(url.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean hasValidURL() {
        String url = externalURLField.getText().trim();
        // empty url is allowed, the URL is not required
        return StringUtils.isBlank(url) || isValidURL(url);
    }

    private void adjustBrowseButton(boolean hasValidURL) {

        browseButton.setEnabled(hasValidURL);
        externalURLField.setForeground(hasValidURL ? textColour : errorColour);
    }

    @Override
    protected List<String> fieldErrors() {
        List<String> errors = new ArrayList<>(3);

        if (StringUtils.isBlank(descriptionBox.getText().trim())) {
            errors.add("Missing description");
        }
        if (StringUtils.isBlank(messageField.getText().trim())) {
            errors.add("Missing message");
        }

        if (!hasValidURL()) {
            errors.add("Invalid external URL");
        }

        return errors;
    }
}
