/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.panelmanagers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.plugin.UISettings;
import net.sourceforge.pmd.eclipse.runtime.builder.MarkerUtil;
import net.sourceforge.pmd.eclipse.ui.LabelProvider;
import net.sourceforge.pmd.eclipse.ui.ShapePicker;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ImplementationType;
import net.sourceforge.pmd.eclipse.ui.preferences.br.RuleFieldAccessor;
import net.sourceforge.pmd.eclipse.ui.preferences.br.RuleSelection;
import net.sourceforge.pmd.eclipse.ui.preferences.br.RuleUtil;
import net.sourceforge.pmd.eclipse.ui.preferences.br.RuleVisitor;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.eclipse.util.internal.SWTUtil;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.lang.rule.RulePriority;
import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.lang.rule.xpath.XPathRule;

/**
 *
 * @author Brian Remedios
 */
public class RulePanelManager extends AbstractRulePanelManager {

    private RuleTarget target;

    private Text nameField;
    private Text implementationClassField;
    private Combo ruleSetNameField;

    private Combo languageCombo;
    private Combo priorityCombo;
    private ShapePicker priorityDisplay;

    private Label minLanguageLabel;
    private Label maxLanguageLabel;
    private Combo minLanguageVersionCombo;
    private Combo maxLanguageVersionCombo;

    private Combo implementationTypeCombo;

    private Button usesTypeResolutionButton;
    private Button usesDfaButton;
    private List<Label> labels;

    private boolean inSetup;
    private Set<String> currentRuleNames;

    public static final String ID = "rule";

    // TODO move to RuleSet class
    public static final Comparator<RuleSet> BY_NAME_COMPARATOR = new Comparator<RuleSet>() {
        @Override
        public int compare(RuleSet rsA, RuleSet rsB) {
            return rsA.getName().compareTo(rsB.getName());
        }
    };

    public RulePanelManager(String theTitle, EditorUsageMode theMode, ValueChangeListener theListener,
            RuleTarget theRuleSource) {
        this(ID, theTitle, theMode, theListener, theRuleSource);
    }

    public RulePanelManager(String theId, String theTitle, EditorUsageMode theMode, ValueChangeListener theListener,
            RuleTarget theRuleSource) {
        super(theId, theTitle, theMode, theListener);

        target = theRuleSource;
    }

    @Override
    public void showControls(boolean flag) {
        nameField.setVisible(flag);
        implementationTypeCombo.setVisible(flag);
        implementationClassField.setVisible(flag);
        ruleSetNameField.setVisible(flag);
        languageCombo.setVisible(flag);
        priorityCombo.setVisible(flag);
        priorityDisplay.setVisible(flag);
        minLanguageVersionCombo.setVisible(flag);
        maxLanguageVersionCombo.setVisible(flag);
        usesDfaButton.setVisible(flag);
        usesTypeResolutionButton.setVisible(flag);
        for (Label label : labels) {
            label.setVisible(flag);
        }
    }

    @Override
    protected void clearControls() {
        nameField.setText("");
        ruleSetNameField.select(-1);
        implementationClassField.setText("");
        ruleSetNameField.setText("");
        languageCombo.select(-1);
        priorityCombo.select(-1);
        priorityDisplay.setItems(null);
        usesDfaButton.setSelection(false);
        usesTypeResolutionButton.setSelection(false);
        clearLanguageVersionCombos();
    }

    private void clearLanguageVersionCombos() {
        SWTUtil.deselectAll(minLanguageVersionCombo);
        SWTUtil.deselectAll(maxLanguageVersionCombo);
    }

    private void showLanguageVersionFields(Language language) {
        int versionCount = language == null ? 0 : language.getVersions().size();

        boolean hasVersions = versionCount > 1;

        minLanguageLabel.setVisible(hasVersions);
        maxLanguageLabel.setVisible(hasVersions);
        minLanguageVersionCombo.setVisible(hasVersions);
        maxLanguageVersionCombo.setVisible(hasVersions);

        if (hasVersions && language != null) {
            List<LanguageVersion> versions = new ArrayList<>();
            versions.add(null); // allow no selection
            versions.addAll(language.getVersions());
            populate(minLanguageVersionCombo, versions);
            populate(maxLanguageVersionCombo, versions);
        }
    }

    private void populate(Combo field, List<LanguageVersion> versions) {
        field.removeAll();
        for (LanguageVersion version : versions) {
            field.add(version == null ? "" : version.getName());
        }
    }

    private Set<Comparable<?>> uniquePriorities() {
        if (rules == null) {
            return Collections.emptySet();
        }
        return RuleUtil.uniqueAspects(rules, RuleFieldAccessor.PRIORITY);
    }

    private String commonLanguageMinVersionName() {
        if (rules == null) {
            return null;
        }

        LanguageVersion version = RuleUtil.commonLanguageMinVersion(rules);
        return version == null ? null : version.getName();
    }

    private String commonLanguageMaxVersionName() {
        if (rules == null) {
            return null;
        }

        LanguageVersion version = (LanguageVersion) RuleUtil.commonAspect(rules,
                RuleFieldAccessor.MAX_LANGUAGE_VERSION);

        return version == null ? null : version.getName();
    }

    private String commonPriorityName() {
        if (rules == null) {
            return null;
        }

        RulePriority priority = RuleUtil.commonPriority(rules);
        return priority == null ? null : UISettings.labelFor(priority);
    }

    @Override
    protected void adapt() {
        show(ruleSetNameField, RuleUtil.commonRuleset(rules));

        Language language = RuleUtil.commonLanguage(rules);
        show(languageCombo, language == null ? "" : language.getName());

        ImplementationType impType = rules == null ? ImplementationType.Mixed : rules.implementationType();
        implementationType(impType);
        implementationTypeCombo.setEnabled(creatingNewRule());

        Class<?> impClass = RuleUtil.commonImplementationClass(rules);
        show(implementationClassField, impClass != null ? impClass.getName() : "");
        implementationClassField.setEnabled(impClass != null);

        show(priorityCombo, commonPriorityName());
        priorityDisplay.setItems(uniquePriorities().toArray());

        showLanguageVersionFields(language);

        show(minLanguageVersionCombo, commonLanguageMinVersionName());
        show(maxLanguageVersionCombo, commonLanguageMaxVersionName());

        Rule soleRule = soleRule();

        if (soleRule == null) {
            shutdown(nameField);
        } else {
            show(nameField, asCleanString(soleRule.getName()));
        }

        validate();
    }

    @Override
    protected boolean canManageMultipleRules() {
        return true;
    }

    @Override
    public Control setupOn(Composite parent) {
        inSetup = true;

        labels = new ArrayList<>();

        Composite dlgArea = new Composite(parent, SWT.NONE);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 6;
        dlgArea.setLayout(gridLayout);

        // put first if we're not creating a new rule
        if (!creatingNewRule()) {
            buildPriorityControls(dlgArea);
        }

        Label nameLabel = buildLabel(dlgArea, StringKeys.PREF_RULEEDIT_LABEL_NAME);
        GridData data = new GridData();
        data.horizontalSpan = 1;
        nameLabel.setLayoutData(data);

        nameField = buildNameText(dlgArea);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 5;
        data.grabExcessHorizontalSpace = true;
        nameField.setLayoutData(data);

        Label ruleSetNameLabel = buildLabel(dlgArea, StringKeys.PREF_RULEEDIT_LABEL_RULESET_NAME);
        data = new GridData();
        data.horizontalSpan = 1;
        ruleSetNameLabel.setLayoutData(data);

        ruleSetNameField = buildRuleSetNameField(dlgArea);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 5;
        data.grabExcessHorizontalSpace = true;
        ruleSetNameField.setLayoutData(data);

        Label implTypeLabel = buildLabel(dlgArea, StringKeys.PREF_RULEEDIT_LABEL_IMPLEMENTED_BY);
        data = new GridData();
        data.horizontalSpan = 1;
        implTypeLabel.setLayoutData(data);

        implementationTypeCombo = buildImplementationTypeCombo(dlgArea);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 5;
        data.grabExcessHorizontalSpace = true;
        implementationTypeCombo.setLayoutData(data);

        Label implementationClassLabel = buildLabel(dlgArea, StringKeys.PREF_RULEEDIT_LABEL_IMPLEMENTATION_CLASS);
        data = new GridData();
        data.horizontalSpan = 1;
        implementationClassLabel.setLayoutData(data);

        implementationClassField = buildImplementationClassField(dlgArea);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 5;
        data.grabExcessHorizontalSpace = true;
        implementationClassField.setLayoutData(data);

        buildLabel(dlgArea, null);
        usesTypeResolutionButton = buildUsesTypeResolutionButton(dlgArea);
        usesDfaButton = buildUsesDfaButton(dlgArea);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 4;
        data.grabExcessHorizontalSpace = true;
        usesDfaButton.setLayoutData(data);
        // buildLabel(dlgArea, null);

        Label languageLabel = buildLabel(dlgArea, StringKeys.PREF_RULEEDIT_LABEL_LANGUAGE);
        data = new GridData();
        data.horizontalSpan = 1;
        languageLabel.setLayoutData(data);

        languageCombo = buildLanguageCombo(dlgArea);
        data = new GridData();
        data.horizontalAlignment = GridData.BEGINNING;
        data.horizontalSpan = 1;
        data.grabExcessHorizontalSpace = false;
        languageCombo.setLayoutData(data);

        GridData lblGD = new GridData();
        lblGD.horizontalSpan = 1;
        lblGD.horizontalAlignment = SWT.END;

        GridData cmboGD = new GridData();
        cmboGD.horizontalAlignment = GridData.FILL;
        cmboGD.horizontalSpan = 1;
        cmboGD.grabExcessHorizontalSpace = true;

        minLanguageLabel = buildLabel(dlgArea, StringKeys.PREF_RULEEDIT_LABEL_LANGUAGE_MIN);
        minLanguageLabel.setAlignment(SWT.RIGHT);
        minLanguageLabel.setLayoutData(lblGD);

        minLanguageVersionCombo = buildLanguageVersionCombo(dlgArea, true);
        minLanguageVersionCombo.setLayoutData(cmboGD);

        maxLanguageLabel = buildLabel(dlgArea, StringKeys.PREF_RULEEDIT_LABEL_LANGUAGE_MAX);
        maxLanguageLabel.setAlignment(SWT.RIGHT);
        maxLanguageLabel.setLayoutData(lblGD);

        maxLanguageVersionCombo = buildLanguageVersionCombo(dlgArea, false);
        maxLanguageVersionCombo.setLayoutData(cmboGD);

        if (creatingNewRule()) {
            buildPriorityControls(dlgArea); // put it at the bottom when creating new rules
            implementationType(ImplementationType.XPath);
        }

        setControl(dlgArea);

        validate();

        inSetup = false;

        return dlgArea;
    }

    private Label buildLabel(Composite parent, String msgKey) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(msgKey == null ? "" : SWTUtil.stringFor(msgKey));
        labels.add(label);
        return label;
    }

    private Text buildNameText(Composite parent) {
        int style = creatingNewRule() ? SWT.SINGLE | SWT.BORDER : SWT.READ_ONLY | SWT.BORDER;
        final Text nameField = new Text(parent, style);
        nameField.setFocus();

        Listener validateListener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                validateRuleParams();
            }
        };

        nameField.addListener(SWT.Modify, validateListener);
        nameField.addListener(SWT.DefaultSelection, validateListener);

        return nameField;
    }

    private Combo buildRuleSetNameField(Composite parent) {
        int style = creatingNewRule() ? SWT.BORDER : SWT.READ_ONLY;
        Combo field = new Combo(parent, style);

        Collection<RuleSet> rs = PMDPlugin.getDefault().getRuleSetManager().getRegisteredRuleSets();
        RuleSet[] ruleSets = rs.toArray(new RuleSet[0]);
        Arrays.sort(ruleSets, BY_NAME_COMPARATOR);
        for (RuleSet ruleSet : ruleSets) {
            field.add(ruleSet.getName().trim());
        }

        Listener validateListener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                validateRuleParams();
            }
        };

        field.addListener(SWT.Modify, validateListener);
        field.addListener(SWT.DefaultSelection, validateListener);

        return field;
    }

    private void implementationType(ImplementationType type) {
        switch (type) {
        case XPath: {
            implementationClassField.setEnabled(false);
            usesTypeResolutionButton.setEnabled(false);
            usesTypeResolutionButton.setSelection(true);
            usesDfaButton.setEnabled(false);
            usesDfaButton.setSelection(false);
            implementationTypeCombo.select(0);
            if (creatingNewRule()) {
                implementationClassField.setText(XPathRule.class.getName());
            }
            break;
        }
        case Java: {
            implementationClassField.setEnabled(true);
            usesTypeResolutionButton.setEnabled(true);
            usesTypeResolutionButton.setSelection(true);
            usesDfaButton.setEnabled(true);
            usesDfaButton.setSelection(false);
            implementationTypeCombo.select(1);
            if (creatingNewRule()) {
                implementationClassField.setText("");
            }
            break;
        }

        case Mixed: {
            implementationTypeCombo.deselectAll();
            break;
        }
        }
        validateRuleParams();
    }

    private Combo buildImplementationTypeCombo(Composite parent) {
        final Combo combo = new Combo(parent, SWT.READ_ONLY);
        combo.add("XPath script");
        combo.add("Java class");

        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                int idx = combo.getSelectionIndex();
                switch (idx) {
                case 0: {
                    implementationType(ImplementationType.XPath);
                    break;
                }
                case 1: {
                    implementationType(ImplementationType.Java);
                    break;
                }
                case -1: {
                    implementationType(ImplementationType.Mixed);
                    break;
                }
                default:
                    throw new IllegalStateException();
                }
            }
        });

        combo.select(0);

        return combo;
    }

    private Combo buildLanguageCombo(Composite parent) {
        final List<Language> languages = new ArrayList<>(LanguageRegistry.PMD.getLanguages());

        final Combo combo = new Combo(parent, SWT.READ_ONLY);

        Language deflt = JavaLanguageModule.getInstance();
        int selectionIndex = -1;

        for (int i = 0; i < languages.size(); i++) {
            if (Objects.equals(languages.get(i), deflt)) {
                selectionIndex = i;
            }
            combo.add(languages.get(i).getName());
        }
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (rules == null) {
                    return;
                }
                Language language = languages.get(combo.getSelectionIndex());
                rules.setLanguage(language);
                updateLanguageVersionComboSelections(language);
                changed(null, language.getName());
            }
        });

        combo.select(selectionIndex);

        return combo;
    }

    private void updateLanguageVersionComboSelections(Language language) {
        List<LanguageVersion> versions = language.getVersions();

        if (versions.size() > 1) {
            showLanguageVersionFields(language);
            show(minLanguageVersionCombo, commonLanguageMinVersionName());
            show(maxLanguageVersionCombo, commonLanguageMaxVersionName());
        } else {
            showLanguageVersionFields(null);
        }
    }

    private Language selectedLanguage() {
        int index = languageCombo.getSelectionIndex();
        if (index < 0) {
            return null; // should never happen!
        }
        final List<Language> languages = new ArrayList<>(LanguageRegistry.PMD.getLanguages());
        return languages.get(index);
    }

    private LanguageVersion selectedVersionIn(Combo versionCombo) {
        int index = versionCombo.getSelectionIndex();
        if (index < 0) {
            return null;
        }
        return selectedLanguage().getVersions().get(index);
    }

    private Combo buildLanguageVersionCombo(Composite parent, final boolean isMinVersion) {
        int style = creatingNewRule() ? SWT.SINGLE | SWT.BORDER : SWT.READ_ONLY | SWT.BORDER;
        final Combo combo = new Combo(parent, style);

        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (rules == null) {
                    return;
                }

                final int selIdx = combo.getSelectionIndex();
                final LanguageVersion version = selIdx == 0 ? null : selectedLanguage().getVersions().get(selIdx - 1);

                RuleVisitor visitor = new RuleVisitor() {
                    @Override
                    public boolean accept(Rule rule) {
                        if (isMinVersion) {
                            rule.setMinimumLanguageVersion(version);
                        } else {
                            rule.setMaximumLanguageVersion(version);
                        }
                        return true;
                    }
                };

                rules.rulesDo(visitor);

                valueChanged(null, version == null ? "" : version.getName());
            }
        });

        return combo;
    }

    private Combo buildPriorityCombo(Composite parent) {
        final Combo combo = new Combo(parent, SWT.READ_ONLY | SWT.BORDER);
        // combo.setEditable(false);
        final RulePriority[] priorities = RulePriority.values();

        for (RulePriority rulePriority : priorities) {
            combo.add(UISettings.labelFor(rulePriority));
        }

        if (rules != null) {
            RulePriority priority = RuleUtil.commonPriority(rules);
            int index = priority == null ? -1 : priority.getPriority() - 1;
            combo.select(index);
        }

        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                setPriority(priorities[combo.getSelectionIndex()]);
                validateRuleParams();
            }
        });

        combo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

        return combo;
    }

    private void setPriority(RulePriority priority) {
        priorityDisplay.setItems(new Object[] { priority });
        if (rules != null) {
            rules.setPriority(priority);
        }
        valueChanged(null, priority);
    }

    private Button buildUsesTypeResolutionButton(Composite parent) {
        final Button button = new Button(parent, SWT.CHECK);
        button.setText(SWTUtil.stringFor(StringKeys.PREF_RULEEDIT_BUTTON_USES_TYPE_RESOLUTION));
        return button;
    }

    private Button buildUsesDfaButton(Composite parent) {
        final Button button = new Button(parent, SWT.CHECK);
        button.setText(SWTUtil.stringFor(StringKeys.PREF_RULEEDIT_BUTTON_USES_DFA));
        return button;
    }

    private boolean hasValidRuleType() {
        if (!implementationClassField.isEnabled()) {
            return true;
        }

        String newType = implementationClassField.getText();
        Class<?> newTypeClass = null;
        try {
            if (newType != null && !newType.isEmpty()) {
                newTypeClass = getClass().getClassLoader().loadClass(newType);
            }
        } catch (ClassNotFoundException e) {
            PMDPlugin.getDefault().logError("Couldn't find rule impl class " + newType, e);
        }
        return newTypeClass != null && Rule.class.isAssignableFrom(newTypeClass);
    }

    private String nameFieldValue() {
        return nameField.getText().trim();
    }

    private void buildPriorityControls(Composite parent) {
        Label priorityLabel = buildLabel(parent, StringKeys.PREF_RULEEDIT_LABEL_PRIORITY);
        GridData data = new GridData();
        data.horizontalSpan = 1;
        priorityLabel.setLayoutData(data);

        priorityCombo = buildPriorityCombo(parent);

        priorityDisplay = new ShapePicker<>(parent, SWT.NONE, 14);
        priorityDisplay.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));
        priorityDisplay.setShapeMap(UISettings.shapesByPriority());
        priorityDisplay.tooltipProvider(new LabelProvider() {
            @Override
            public String labelFor(Object item) {
                return UISettings.labelFor((RulePriority) item);
            }
        });
        priorityDisplay.setSize(120, 25);
    }

    private boolean hasValidRuleName() {
        if (creatingNewRule() && !isValidRuleName(nameFieldValue())) {
            return false;
        }

        if (rules == null || rules.hasMultipleRules()) {
            return true;
        }

        return isValidRuleName(nameFieldValue());
    }

    private boolean hasExistingRuleName() {
        if (currentRuleNames == null) {
            currentRuleNames = MarkerUtil.currentRuleNames();
        }
        return currentRuleNames.contains(nameFieldValue());
    }

    private boolean hasValidRulesetName() {
        String name = ruleSetNameField.getText();
        return isValidRulesetName(name);
    }

    private static boolean hasNoSelection(Combo combo) {
        return combo.getSelectionIndex() < 0;
    }

    private boolean hasValidChoice(Combo combo) {
        if (creatingNewRule() && hasNoSelection(combo)) {
            return false;
        }
        if (rules == null || rules.hasMultipleRules()) {
            return true;
        }
        return priorityCombo.getSelectionIndex() >= 0;
    }

    @Override
    protected List<String> fieldErrors() {
        List<String> errors = new ArrayList<>();

        if (!hasValidRuleType()) {
            errors.add("Invalid rule class");
        }
        if (!hasValidRuleName()) {
            errors.add("Invalid rule name");
        }
        if (creatingNewRule() && hasExistingRuleName()) {
            errors.add("Rule name is already in use");
        }
        if (!hasValidRulesetName()) {
            errors.add("Invalid ruleset name");
        }
        if (!hasValidChoice(priorityCombo)) {
            errors.add("No priority selected");
        }
        if (!hasValidChoice(languageCombo)) {
            errors.add("No language selected");
        }

        return errors;
    }

    private void validateRuleParams() {
        boolean isOk = validate();

        if (isOk && creatingNewRule()) {
            populateRuleInstance();
        }

        if (inSetup) {
            return;
        }

        if (target != null) {
            target.rule(isOk ? rules.soleRule() : null);
        }
    }

    private void copyLocalValuesTo(Rule rule) {
        rule.setName(nameFieldValue());
        rule.setRuleSetName(ruleSetNameField.getText());

        Language language = selectedLanguage();
        rule.setLanguage(language);

        rule.setPriority(RulePriority.valueOf(priorityCombo.getSelectionIndex() + 1));
        rule.setMinimumLanguageVersion(selectedVersionIn(minLanguageVersionCombo));
        rule.setMaximumLanguageVersion(selectedVersionIn(maxLanguageVersionCombo));
    }

    private void populateRuleInstance() {
        String ruleType = implementationClassField.getText();

        try {
            Class<Rule> ruleTypeClass = (Class<Rule>) getClass().getClassLoader().loadClass(ruleType);
            Rule newRule = ruleTypeClass.getConstructor().newInstance();

            if (rules == null) {
                rules = new RuleSelection(newRule);
            } else {
                if (newRule.getClass() != soleRule().getClass()) {
                    rules.soleRule(newRule);
                }
            }

            copyLocalValuesTo(rules.soleRule());

        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Text buildImplementationClassField(Composite parent) {
        int style = creatingNewRule() ? SWT.SINGLE | SWT.BORDER : SWT.READ_ONLY | SWT.BORDER;
        final Text classField = new Text(parent, style);

        classField.setEnabled(false);

        Listener validateListener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                validateRuleParams();
            }
        };

        classField.addListener(SWT.FocusOut, validateListener);
        classField.addListener(SWT.DefaultSelection, validateListener);

        return classField;
    }

    private static boolean isValidRuleName(String candidateName) {
        // TODO
        return !StringUtils.isBlank(candidateName);
    }

    private static boolean isValidRulesetName(String candidateName) {
        // TODO
        return !StringUtils.isBlank(candidateName);
    }
}
