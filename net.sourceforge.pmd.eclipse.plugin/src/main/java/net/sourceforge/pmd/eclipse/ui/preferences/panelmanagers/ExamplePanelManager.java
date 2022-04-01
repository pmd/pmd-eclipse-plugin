/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.panelmanagers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.eclipse.ui.editors.SyntaxManager;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.eclipse.util.internal.StringUtil;
import net.sourceforge.pmd.lang.rule.RuleReference;

/**
 *
 * @author Brian Remedios
 */
public class ExamplePanelManager extends AbstractRulePanelManager {

    private StyledText exampleField;
    private ModifyListener modifyListener;

    public static final String ID = "example";

    public ExamplePanelManager(String theTitle, EditorUsageMode theMode, ValueChangeListener theListener) {
        super(ID, theTitle, theMode, theListener);
    }

    @Override
    protected boolean canManageMultipleRules() {
        return false;
    }

    @Override
    protected void clearControls() {
        exampleField.setText("");
    }

    @Override
    public void showControls(boolean flag) {
        exampleField.setVisible(flag);
    }

    @Override
    protected void updateOverridenFields() {
        Rule rule = soleRule();

        if (rule instanceof RuleReference) {
            RuleReference ruleReference = (RuleReference) rule;
            exampleField.setBackground(ruleReference.getOverriddenExamples() != null ? overridenColour : null);
        }
    }

    @Override
    public Control setupOn(Composite parent) {
        GridData gridData;

        Composite panel = new Composite(parent, 0);
        GridLayout layout = new GridLayout(2, false);
        panel.setLayout(layout);

        exampleField = newCodeField(panel);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 1;
        exampleField.setLayoutData(gridData);

        exampleField.addListener(SWT.FocusOut, new Listener() {
            @Override
            public void handleEvent(Event event) {
                Rule soleRule = soleRule();

                String cleanValue = exampleField.getText().trim();
                String existingValue = soleRule.getDescription();

                if (StringUtils.equals(StringUtils.stripToNull(existingValue), StringUtils.stripToNull(cleanValue))) {
                    return;
                }

                soleRule.setDescription(cleanValue);
                valueChanged(null, cleanValue);
            }
        });

        return panel;
    }

    private void formatExampleOn(StringBuilder sb, String example) {
        // sb.append(example.trim());

        String[] lines = example.split("\n");
        List<String> realLines = new ArrayList<>(lines.length);
        for (String line : lines) {
            if (StringUtils.isNotBlank(line)) {
                realLines.add(line);
            }
        }
        lines = realLines.toArray(new String[0]);

        int trimDepth = StringUtil.maxCommonLeadingWhitespaceForAll(lines);
        if (trimDepth > 0) {
            lines = StringUtil.trimStartOn(lines, trimDepth);
        }
        for (String line : lines) {
            sb.append(line).append(System.lineSeparator());
        }
    }

    private String examples(Rule rule) {
        List<String> examples = rule.getExamples();
        if (examples.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        formatExampleOn(sb, examples.get(0));

        for (int i = 1; i < examples.size(); i++) {
            sb.append("----------");
            formatExampleOn(sb, examples.get(i));
        }

        return sb.toString();
    }

    @Override
    protected void adapt() {
        Rule soleRule = soleRule();

        if (soleRule == null) {
            shutdown(exampleField);
        } else {
            show(exampleField, examples(soleRule));
            modifyListener = SyntaxManager.adapt(exampleField, soleRule.getLanguage().getTerseName(), modifyListener);
        }
    }
}
