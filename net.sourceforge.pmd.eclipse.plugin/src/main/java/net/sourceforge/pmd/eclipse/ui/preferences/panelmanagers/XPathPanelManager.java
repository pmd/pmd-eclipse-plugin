/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.panelmanagers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.sourceforge.pmd.eclipse.ui.editors.SyntaxManager;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ImplementationType;
import net.sourceforge.pmd.eclipse.ui.preferences.br.RuleSelection;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.lang.rule.RuleReference;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 *
 * @author Brian Remedios
 */
public class XPathPanelManager extends AbstractRulePanelManager {

    private StyledText xpathField;
    private List<String> unknownVariableNames;

    public static final String ID = "xpath";

    public XPathPanelManager(String theTitle, EditorUsageMode theMode, ValueChangeListener theListener) {
        super(ID, theTitle, theMode, theListener);
    }

    @Override
    protected boolean canManageMultipleRules() {
        return false;
    }

    @Override
    protected boolean canWorkWith(Rule rule) {
        return RuleSelection.implementationType(rule) == ImplementationType.XPath;
    }

    @Override
    protected List<String> fieldErrors() {
        List<String> errors = new ArrayList<>(2);

        if (StringUtils.isBlank(xpathField.getText())) {
            errors.add("Missing XPATH code");
        }

        if (unknownVariableNames == null || unknownVariableNames.isEmpty()) {
            return errors;
        }

        errors.add("Unknown variables: " + unknownVariableNames);

        return errors;
    }

    @Override
    protected void clearControls() {
        xpathField.setText("");
    }

    @Override
    public void showControls(boolean flag) {
        xpathField.setVisible(flag);
    }

    private PropertyDescriptor<String> getXPathDescriptor(PropertySource propertySource) {
        return (PropertyDescriptor<String>) propertySource.getPropertyDescriptor(Configuration.XPATH_EXPRESSION_PROPERTY);
    }

    @Override
    protected void updateOverridenFields() {
        Rule rule = soleRule();

        if (rule instanceof RuleReference) {
            RuleReference ruleReference = (RuleReference) rule;
            xpathField.setBackground(
                    ruleReference.isPropertyOverridden(getXPathDescriptor(ruleReference)) ? overridenColour : null);
        }
    }

    @Override
    public Control setupOn(Composite parent) {
        GridData gridData;

        Composite panel = new Composite(parent, 0);
        GridLayout layout = new GridLayout(2, false);
        panel.setLayout(layout);

        xpathField = newCodeField(panel);
        SyntaxManager.adapt(xpathField, "xpath", null);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 2;
        xpathField.setLayoutData(gridData);

        xpathField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {

                if (!isActive()) {
                    return;
                }

                Rule soleRule = soleRule();
                if (soleRule == null) {
                    return;
                }

                String newValue = xpathField.getText().trim();
                PropertyDescriptor<String> xpathDescriptor = getXPathDescriptor(soleRule);
                String existingValue = soleRule.getProperty(xpathDescriptor).trim();

                if (StringUtils.equals(StringUtils.stripToNull(existingValue), StringUtils.stripToNull(newValue))) {
                    return;
                }

                validate();
                soleRule.setProperty(xpathDescriptor, newValue);

                // updateVariablesField();
                valueChanged(xpathDescriptor, newValue);
            }
        });

        return panel;
    }

    // private static StyleRange styleFor(Rule rule, String source, int[] position, List<String> unknownVars) {
    //
    // String varName = source.substring(position[0], position[0] + position[1]);
    // PropertyDescriptor<?> desc = rule.getPropertyDescriptor(varName);
    //
    // if (desc == null) unknownVars.add(varName);
    //
    // return new StyleRange(
    // position[0], position[1],
    // desc == null ? errorColour : null,
    // null,
    // SWT.BOLD
    // );
    // }
    //
    // private void updateVariablesField() {
    //
    // xpathField.setStyleRange(null); // clear all
    //
    // Rule rule = soleRule();
    // unknownVariableNames = new ArrayList<String>();
    //
    // String xpath = rule.getProperty(XPathRule.XPATH_DESCRIPTOR).trim();
    // List<int[]> positions = Util.referencedNamePositionsIn(xpath, '$');
    // for (int[] position : positions) {
    // StyleRange range = styleFor(rule, xpath, position, unknownVariableNames);
    // xpathField.setStyleRange(range);
    // }
    // }

    @Override
    public void adapt() {
        Rule soleRule = soleRule();

        if (soleRule == null) {
            shutdown(xpathField);
        } else {
            PropertyDescriptor<String> xpathDescriptor = getXPathDescriptor(soleRule);
            show(xpathField, soleRule.getProperty(xpathDescriptor).trim());
            // updateVariablesField();
        }

        validate();
    }
}
