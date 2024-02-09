/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.panelmanagers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.sourceforge.pmd.eclipse.ui.preferences.br.EditorFactory;
import net.sourceforge.pmd.eclipse.ui.preferences.br.RuleSelection;
import net.sourceforge.pmd.eclipse.ui.preferences.br.RuleUtil;
import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.editors.BooleanEditorFactory;
import net.sourceforge.pmd.eclipse.ui.preferences.editors.CharacterEditorFactory;
import net.sourceforge.pmd.eclipse.ui.preferences.editors.DoubleEditorFactory;
import net.sourceforge.pmd.eclipse.ui.preferences.editors.EnumerationEditorFactory;
import net.sourceforge.pmd.eclipse.ui.preferences.editors.IntegerEditorFactory;
import net.sourceforge.pmd.eclipse.ui.preferences.editors.MultiIntegerEditorFactory;
import net.sourceforge.pmd.eclipse.ui.preferences.editors.MultiStringEditorFactory;
import net.sourceforge.pmd.eclipse.ui.preferences.editors.RegexEditorFactory;
import net.sourceforge.pmd.eclipse.ui.preferences.editors.StringEditorFactory;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 *
 * @author Brian Remedios
 */
public class PerRulePropertyPanelManager extends AbstractRulePanelManager implements SizeChangeListener {

    private FormArranger formArranger;
    private Composite composite;
    private ScrolledComposite sComposite;
    private int widgetRowCount;
    private List<String> unreferencedVariables;

    private static final int MAX_WIDGET_HEIGHT = 30; // TODO derive this instead
    public static final String ID = "perRuleProperties";

    public static final Map<Class<?>, EditorFactory<?>> EDITOR_FACTORIES_BY_PROPERTY_TYPE;

    static {
        Map<Class<?>, EditorFactory<?>> factoriesByPropertyType = new HashMap<>();

        factoriesByPropertyType.put(Boolean.class, BooleanEditorFactory.INSTANCE);
        factoriesByPropertyType.put(Character.class, CharacterEditorFactory.INSTANCE);
        factoriesByPropertyType.put(Double.class, DoubleEditorFactory.INSTANCE);
        factoriesByPropertyType.put(Integer.class, IntegerEditorFactory.INSTANCE);
        //Long is missing
        factoriesByPropertyType.put(Pattern.class, RegexEditorFactory.INSTANCE);
        factoriesByPropertyType.put(String.class, StringEditorFactory.INSTANCE);
        factoriesByPropertyType.put(Object.class, EnumerationEditorFactory.INSTANCE);

        //CharList is missing
        //DoubleList is missing
        factoriesByPropertyType.put(Integer[].class, MultiIntegerEditorFactory.INSTANCE);
        //LongList is missing
        factoriesByPropertyType.put(String[].class, MultiStringEditorFactory.INSTANCE);
        factoriesByPropertyType.put(Object[].class, MultiStringEditorFactory.INSTANCE); // enum list

        EDITOR_FACTORIES_BY_PROPERTY_TYPE = Collections.unmodifiableMap(factoriesByPropertyType);
    }

    public PerRulePropertyPanelManager(String theTitle, EditorUsageMode theMode, ValueChangeListener theListener) {
        super(ID, theTitle, theMode, theListener);
    }

    @Override
    protected boolean canManageMultipleRules() {
        return false;
    }

    @Override
    protected boolean canWorkWith(Rule rule) {
        return RuleUtil.isXPathRule(rule) || !Configuration.filteredPropertiesOf(rule).isEmpty();
    }

    @Override
    protected void clearControls() {
        formArranger.clearChildren();
    }

    @Override
    public void loadValues() {
        formArranger.loadValues();
    }

    @Override
    public void showControls(boolean flag) {
        clearControls();
    }

    /*
     * We want to intercept this and update the tab if we detect problems after
     * we pass it on..
     */
    private ValueChangeListener chainedListener() {
        return FormArranger.chain(changeListener, new ValueChangeListener() {
            @Override
            public void changed(RuleSelection rule, PropertyDescriptor<?> desc, Object newValue) {
                updateUI();
            }

            @Override
            public void changed(PropertySource source, PropertyDescriptor<?> desc, Object newValue) {
                updateUI();
            }

        });
    }

    @Override
    public Control setupOn(Composite parent) {
        sComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        composite = new Composite(sComposite, SWT.NONE);

        sComposite.setContent(composite);
        sComposite.setExpandHorizontal(true);
        sComposite.setExpandVertical(true);

        formArranger = new FormArranger(composite, EDITOR_FACTORIES_BY_PROPERTY_TYPE, chainedListener(), this);

        return sComposite;
    }

    @Override
    public void addedRows(int rowCountDelta) {
        widgetRowCount += rowCountDelta;
        adjustMinimumHeight();
    }

    private void adjustMinimumHeight() {
        sComposite.setMinSize(composite.computeSize(500, widgetRowCount * MAX_WIDGET_HEIGHT));
    }

    @Override
    protected void adapt() {
        widgetRowCount = formArranger.arrangeFor(soleRule());
        validate();

        if (widgetRowCount < 0) {
            return;
        }

        adjustMinimumHeight();
    }

    @Override
    public boolean validate() {
        if (!super.validate()) {
            return false;
        }

        // any unref'd vars are not real errors
        unreferencedVariables = formArranger.updateDeleteButtons(); 

        return true;
    }

    @Override
    protected List<String> fieldWarnings() {
        List<String> warnings = new ArrayList<>(2);

        if (rules != null && !canManageMultipleRules()) { // TODO can do better
            Rule soleRule = soleRule();
            if (soleRule != null) {
                String dysfunctionReason = soleRule.dysfunctionReason();
                if (dysfunctionReason != null) {
                    warnings.add(dysfunctionReason);
                }
            }
        }

        if (unreferencedVariables == null || unreferencedVariables.isEmpty()) {
            return warnings;
        }

        warnings.add("Unreferences variables: " + unreferencedVariables);

        return warnings;
    }
}
