/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.dialogs;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.preferences.br.EditorFactory;
import net.sourceforge.pmd.eclipse.ui.preferences.br.RuleUIUtil;
import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.editors.SWTUtil;
import net.sourceforge.pmd.eclipse.util.Util;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.lang.rule.RuleReference;
import net.sourceforge.pmd.lang.rule.xpath.XPathRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * Implements a dialog for adding or editing a rule property. As the user
 * changes the specified type, each type's associated editor factory will
 * provide additional labels & widgets intended to capture other metadata.
 *
 * @author Brian Remedios
 */
public class NewPropertyDialog extends TitleAreaDialog implements SizeChangeListener {

    private Text nameField;
    private Text labelField;
    private Combo typeField;
    private Control[] factoryControls;
    private Composite dlgArea;
    private EditorFactory<?> factory;
    private ValueChangeListener changeListener;

    private PropertySource propertySource;
    private PropertyDescriptor<?> descriptor;
    private Map<Class<?>, EditorFactory<?>> editorFactoriesByValueType;

    // these are the ones we've tested, the others may work but might not make
    // sense in the xpath source context...
    private static final Class<?>[] VALID_EDITOR_TYPES = new Class[] { String.class, Integer.class, Boolean.class,
        Class.class, Method.class };
    private static final Class<?> DEFAULT_EDITOR_TYPE = VALID_EDITOR_TYPES[0]; // first one

    /**
     * Constructor for RuleDialog. Supply a working descriptor with name &
     * description values we expect the user to change.
     *
     * @param parentdlgArea
     */
    public NewPropertyDialog(Shell parent, Map<Class<?>, EditorFactory<?>> theEditorFactoriesByValueType,
            PropertySource theSource, ValueChangeListener theChangeListener) {
        super(parent);

        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);

        propertySource = theSource;
        changeListener = theChangeListener;
        editorFactoriesByValueType = withOnly(theEditorFactoriesByValueType, VALID_EDITOR_TYPES);
    }

    /**
     * Constructor for RuleDialog.
     *
     * @param parentdlgArea
     */
    public NewPropertyDialog(Shell parent, Map<Class<?>, EditorFactory<?>> theEditorFactoriesByValueType, Rule theRule,
            PropertyDescriptor<?> theDescriptor, ValueChangeListener theChangeListener) {
        this(parent, theEditorFactoriesByValueType, theRule, theChangeListener);

        descriptor = theDescriptor;
    }

    public static Map<Class<?>, EditorFactory<?>> withOnly(Map<Class<?>, EditorFactory<?>> factoriesByType,
            Class<?>[] legalTypeKeys) {
        Map<Class<?>, EditorFactory<?>> results = new HashMap<>(legalTypeKeys.length);

        for (Class<?> type : legalTypeKeys) {
            if (factoriesByType.containsKey(type)) {
                results.put(type, factoriesByType.get(type));
            }
        }
        return results;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // parent.setLayoutData(new GridData(GridData.FILL_BOTH));
        getShell().setText(SWTUtil.stringFor(StringKeys.DIALOG_PREFS_ADD_NEW_PROPERTY));

        dlgArea = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(2, false);
        layout.verticalSpacing = 2;
        layout.marginTop = 1;
        dlgArea.setLayout(layout);
        dlgArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        buildLabel(dlgArea, "Name:");
        nameField = buildNameText(dlgArea); // TODO i18l label
        buildLabel(dlgArea, "Datatype:");
        typeField = buildTypeField(dlgArea); // TODO i18l label
        buildLabel(dlgArea, "Label:");
        labelField = buildLabelField(dlgArea); // TODO i18l label

        setPreferredName();
        setInitialType();

        dlgArea.pack();

        return dlgArea;
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control result = super.createButtonBar(parent);
        validateForm();
        return result;
    }

    /**
     * Build a label.
     */
    private Label buildLabel(Composite parent, String msgKey) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(msgKey == null ? "" : SWTUtil.stringFor(msgKey));
        return label;
    }

    private void setFieldLayoutData(Control widget) {

        GridData data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;

        widget.setLayoutData(data);
    }

    /**
     * Build the rule name text.
     */
    private Text buildNameText(Composite parent) {

        final Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
        setFieldLayoutData(text);

        text.addVerifyListener(RuleUIUtil.RULE_NAME_VERIFIER);

        text.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                validateForm();
            }
        });

        return text;
    }

    private boolean isValidNewLabel(String labelCandidate) {
        return StringUtils.isNotBlank(labelCandidate);
    }

    private boolean isPreExistingLabel(String labelCandidate) {

        for (PropertyDescriptor<?> desc : propertySource.getPropertyDescriptors()) {
            if (desc.description().equalsIgnoreCase(labelCandidate)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Build the rule name text.
     */
    private Text buildLabelField(Composite parent) {

        final Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
        setFieldLayoutData(text);

        text.addVerifyListener(RuleUIUtil.RULE_LABEL_VERIFIER);

        text.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                validateForm();
            }
        });

        return text;
    }

    private static String labelFor(Class<?> type) {
        return Util.signatureFor(type, new String[0]);
    }

    /**
     * A bit of a hack but this avoids the need to create an alternate lookup
     * structure.
     *
     * @param label
     * @return
     */
    private EditorFactory<?> factoryFor(String label) {

        for (Entry<Class<?>, EditorFactory<?>> entry : editorFactoriesByValueType.entrySet()) {
            if (label.equals(labelFor(entry.getKey()))) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Build the rule name text.
     */
    private Combo buildTypeField(final Composite parent) {

        final Combo combo = new Combo(parent, SWT.READ_ONLY);
        setFieldLayoutData(combo);

        String[] labels = new String[editorFactoriesByValueType.size()];
        int i = 0;
        for (Entry<Class<?>, EditorFactory<?>> entry : editorFactoriesByValueType.entrySet()) {
            labels[i++] = labelFor(entry.getKey());
        }
        Arrays.sort(labels);
        combo.setItems(labels);

        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectionIdx = combo.getSelectionIndex();
                EditorFactory<?> factory = factoryFor(combo.getItem(selectionIdx));

                factory(factory);
            }
        });

        return combo;
    }

    private boolean ruleHasPropertyName(String name) {
        return propertySource.getPropertyDescriptor(name) != null;
    }

    private static XPathRule toXPathRule(PropertySource propertySource) {
        PropertySource rule = propertySource;
        if (propertySource instanceof RuleReference) {
            rule = ((RuleReference) propertySource).getRule();
        }
        if (rule instanceof XPathRule) {
            return (XPathRule) rule;
        }
        return null;
    }

    /**
     * Pick the first name in the xpath source the rule doesn't know about.
     */
    private void setPreferredName() {
        String xpath = "";
        XPathRule rule = toXPathRule(propertySource);
        if (rule != null) {
            xpath = rule.getXPathExpression().trim();
        }

        List<int[]> positions = Util.referencedNamePositionsIn(xpath, '$');
        List<String> names = Util.fragmentsWithin(xpath, positions);

        nameField.setText("");

        for (String name : names) {
            if (ruleHasPropertyName(name)) {
                continue;
            } else if (nameField.getText().isEmpty()) {
                nameField.setText(name);
            } else {
                break;
            }
        }
    }

    private void setInitialType() {

        String editorLabel = labelFor(DEFAULT_EDITOR_TYPE);
        typeField.select(Util.indexOf(typeField.getItems(), editorLabel));
        factory(factoryFor(editorLabel));
    }

    private void cleanFactoryStuff() {
        if (factoryControls != null) {
            for (Control control : factoryControls) {
                control.dispose();
            }
        }
    }

    private <T> void factory(EditorFactory<T> theFactory) {

        factory = theFactory;
        // dummy values (??) that will be replaced
        PropertyDescriptor<T> theDescriptor = theFactory.createDescriptor("??", "??", null);
        descriptor = theDescriptor;
        labelField.setText(theDescriptor.description());
        cleanFactoryStuff();

        factoryControls = theFactory.createOtherControlsOn(dlgArea, theDescriptor, propertySource, changeListener, this);

        dlgArea.getShell().layout();
        dlgArea.pack();
        dlgArea.getParent().pack();
    }

    // /**
    // * Helper method to shorten message access
    // *
    // * @param key a message key
    // * @return requested message
    // */
    // private String getMessage(String key) {
    // return PMDPlugin.getDefault().getStringTable().getString(key);
    // }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        if (validateForm()) {
            descriptor = newDescriptor();
            super.okPressed();
        }
    }

    /**
     * Perform the form validation
     */
    private boolean validateForm() {
        boolean isOk = validateName() && validateLabel();
        Control button = getButton(IDialogConstants.OK_ID);
        if (button != null) {
            button.setEnabled(isOk);
        }
        return isOk;
    }

    /**
     * Perform the name validation
     */
    private boolean validateName() {

        String name = nameField.getText().trim();

        if (StringUtils.isBlank(name)) {
            setErrorMessage("A property name is required");
            return false;
        }

        if (ruleHasPropertyName(name)) {
            setErrorMessage("'" + name + "' is already used by another property");
            return false;
        }

        setErrorMessage(null);

        return true;
    }

    /**
     * Perform the label validation
     */
    private boolean validateLabel() {

        String label = labelField.getText().trim();

        if (StringUtils.isBlank(label)) {
            setErrorMessage("A descriptive label is required");
            return false;
        }

        if (!isValidNewLabel(label)) {
            setErrorMessage("Invalid label");
            return false;
        }

        if (!isPreExistingLabel(label)) {
            setErrorMessage("Label text must differ from other label text");
            return false;
        }

        setErrorMessage(null);

        return true;
    }

    /**
     * Returns the descriptor.
     *
     * @return PropertyDescriptor
     */
    public PropertyDescriptor<?> descriptor() {
        return descriptor;
    }

    private PropertyDescriptor<?> newDescriptor() {

        return factory.createDescriptor(nameField.getText().trim(), labelField.getText().trim(), factoryControls);
    }

    @Override
    protected void cancelPressed() {
        // courierFont.dispose();
        super.cancelPressed();
    }

    @Override
    public void addedRows(int newRowCount) {
        dlgArea.pack();
        dlgArea.getParent().pack();

        System.out.println("rows added: " + newRowCount);
    }
}
