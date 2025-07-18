/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.PropertyPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;
import net.sourceforge.pmd.eclipse.ui.actions.internal.InternalRuleSetUtil;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.preferences.RuleLabelProvider;
import net.sourceforge.pmd.eclipse.ui.preferences.RuleSetContentProvider;
import net.sourceforge.pmd.eclipse.ui.preferences.RuleTableViewerSorter;
import net.sourceforge.pmd.eclipse.ui.preferences.panelmanagers.SummaryPanelManager;
import net.sourceforge.pmd.eclipse.util.internal.SWTUtil;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.lang.rule.RuleSet;

/**
 * Property page to enable or disable PMD on a project
 *
 * @author Philippe Herlin
 * @author Brian Remedios
 */
public class PMDProjectPropertyPage extends PropertyPage {
    private static final String PROPERTY_LANGUAGE = "language";
    private static final String PROPERTY_RULESET_NAME = "ruleSetname";
    private static final String PROPERTY_RULE_NAME = "ruleName";
    //private static final String PROPERTY_SINCE = "since";
    private static final String PROPERTY_PRIORITY = "priority";
    //private static final String PROPERTY_DESCRIPTION = "description";

    private PMDPropertyPageController controller;
    private PMDPropertyPageBean model;
    private Button enablePMDButton;
    protected TableViewer availableRulesTableViewer;
    private IWorkingSet selectedWorkingSet;
    private Label selectedWorkingSetLabel;
    private Button deselectWorkingSetButton;
    private Button includeDerivedFilesButton;
    private Button violationsAsErrorsButton;
    private Button fullBuildEnabledButton;
    private Button ruleSetStoredInProjectButton;
    private Text ruleSetFileText;
    private Button ruleSetBrowseButton;

    private SummaryPanelManager ruleDescriptionField;
    private Control ruleDescriptionPanel;

    private Collection<Control> activeControls = new ArrayList<>();

    private final RuleTableViewerSorter availableRuleTableViewerSorter = new RuleTableViewerSorter(
            RuleTableViewerSorter.RULE_DEFAULT_COMPARATOR);

    private static final Logger LOG = LoggerFactory.getLogger(PMDProjectPropertyPage.class);

    @Override
    protected Control createContents(final Composite parent) {
        LOG.info("PMD properties editing requested");
        controller = new PMDPropertyPageController(getShell());
        final IProject project = (IProject) getElement().getAdapter(IProject.class);
        controller.setProject(project);
        model = controller.getPropertyPageBean();

        Composite composite = null;
        noDefaultAndApplyButton();

        if (project.isAccessible() && model != null) {
            composite = new Composite(parent, SWT.NONE);

            final GridLayout layout = new GridLayout();
            composite.setLayout(layout);

            enablePMDButton = buildEnablePMDButton(composite);

            Label separator = new Label(composite, SWT.SEPARATOR | SWT.SHADOW_IN | SWT.HORIZONTAL);
            GridData data = new GridData();
            data.horizontalAlignment = GridData.FILL;
            data.grabExcessHorizontalSpace = true;
            separator.setLayoutData(data);

            includeDerivedFilesButton = buildIncludeDerivedFilesButton(composite);

            separator = new Label(composite, SWT.SEPARATOR | SWT.SHADOW_IN | SWT.HORIZONTAL);
            data = new GridData();
            data.horizontalAlignment = GridData.FILL;
            data.grabExcessHorizontalSpace = true;
            separator.setLayoutData(data);

            violationsAsErrorsButton = buildViolationsAsErrorsButton(composite);
            fullBuildEnabledButton = buildFullBuildEnabledButton(composite);

            separator = new Label(composite, SWT.SEPARATOR | SWT.SHADOW_IN | SWT.HORIZONTAL);
            data = new GridData();
            data.horizontalAlignment = GridData.FILL;
            data.grabExcessHorizontalSpace = true;
            separator.setLayoutData(data);

            selectedWorkingSetLabel = buildSelectedWorkingSetLabel(composite);
            data = new GridData();
            data.horizontalAlignment = GridData.FILL;
            data.grabExcessHorizontalSpace = true;
            selectedWorkingSetLabel.setLayoutData(data);

            Composite workingSetPanel = new Composite(composite, SWT.NONE);
            RowLayout rowLayout = new RowLayout();
            rowLayout.type = SWT.HORIZONTAL;
            rowLayout.justify = true;
            rowLayout.pack = false;
            rowLayout.wrap = false;
            workingSetPanel.setLayout(rowLayout);
            buildSelectWorkingSetButton(workingSetPanel);
            deselectWorkingSetButton = buildDeselectWorkingSetButton(workingSetPanel);

            separator = new Label(composite, SWT.SEPARATOR | SWT.SHADOW_IN | SWT.HORIZONTAL);
            data = new GridData();
            data.horizontalAlignment = GridData.FILL;
            data.grabExcessHorizontalSpace = true;
            separator.setLayoutData(data);

            Group ruleSourceGroup = new Group(composite, SWT.BORDER);
            ruleSourceGroup.setText("Rule source");
            activeControls.add(ruleSourceGroup);

            data = new GridData();
            data.grabExcessHorizontalSpace = true;
            data.grabExcessVerticalSpace = true;
            data.horizontalAlignment = GridData.FILL;
            data.verticalAlignment = GridData.FILL;
            ruleSourceGroup.setLayoutData(data);

            ruleSourceGroup.setLayout(new GridLayout());

            final Composite ruleSetPanel = new Composite(ruleSourceGroup, SWT.NONE);
            GridLayout gridLayout = new GridLayout();
            gridLayout.numColumns = 3;
            ruleSetPanel.setLayout(gridLayout);
            data = new GridData();
            data.grabExcessHorizontalSpace = true;
            data.horizontalAlignment = GridData.FILL;
            ruleSetPanel.setLayoutData(data);

            ruleSetStoredInProjectButton = buildStoreRuleSetInProjectButton(ruleSetPanel);
            ruleSetFileText = buildRuleSetFileText(ruleSetPanel);
            ruleSetBrowseButton = buildRuleSetBrowseButton(ruleSetPanel);
            buildLocalRulesButton(ruleSetPanel);

            data = new GridData(SWT.FILL, SWT.NONE, true, false);
            ruleSetFileText.setLayoutData(data);

            final Composite ruleTablePanel = new Composite(ruleSourceGroup, SWT.NONE);
            gridLayout = new GridLayout(2, false);
            ruleTablePanel.setLayout(gridLayout);
            data = new GridData();
            data.grabExcessHorizontalSpace = true;
            data.horizontalAlignment = GridData.FILL;
            data.widthHint = 230;
            ruleTablePanel.setLayoutData(data);

            // buildLabel(composite, StringKeys.PROPERTY_LABEL_SELECT_RULE);
            final Table availableRulesTable = buildAvailableRulesTableViewer(ruleTablePanel);
            data = new GridData();
            // data.grabExcessVerticalSpace = true;
            // data.horizontalAlignment = GridData.FILL;
            data.verticalAlignment = GridData.FILL;
            data.heightHint = 250;
            availableRulesTable.setLayoutData(data);

            ruleDescriptionField = new SummaryPanelManager("asdf", "asdf", null, null);
            ruleDescriptionPanel = ruleDescriptionField.setupOn(ruleTablePanel);
            data = new GridData();
            data.grabExcessHorizontalSpace = true;
            data.widthHint = 200;
            data.horizontalAlignment = GridData.FILL;
            data.verticalAlignment = GridData.FILL;
            ruleDescriptionPanel.setLayoutData(data);

            adjustControls();
        } else {
            setValid(false);
        }

        LOG.debug("Property page created");
        return composite;
    }

    private void showDescriptionField(boolean flag) {

        GridData data = (GridData) ruleDescriptionPanel.getLayoutData();
        data.exclude = !flag;

        ruleDescriptionField.setVisible(flag);
    }

    private void adjustControls() {

        boolean isEnabled = enablePMDButton.getSelection();
        SWTUtil.setEnabled(activeControls, isEnabled);

        if (isEnabled) {
            deselectWorkingSetButton.setEnabled(selectedWorkingSet != null);
            refreshRuleSetInProject();
        }
    }

    private Button newCheckButton(Composite parent, String labelKey) {
        Button button = new Button(parent, SWT.CHECK);
        button.setText(getMessage(labelKey));
        activeControls.add(button);
        return button;
    }

    private Button newRadioButton(Composite parent, String labelKey) {
        Button button = new Button(parent, SWT.RADIO);
        button.setText(getMessage(labelKey));
        activeControls.add(button);
        return button;
    }

    /**
     * Create the enable PMD checkbox.
     * 
     * @param parent
     *            the parent composite
     */
    private Button buildEnablePMDButton(Composite parent) {
        Button button = new Button(parent, SWT.CHECK);
        button.setText(getMessage(StringKeys.PROPERTY_BUTTON_ENABLE));
        button.setSelection(model.isPmdEnabled());

        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                adjustControls();
            }
        });

        return button;
    }

    /**
     * Create the include derived files checkbox.
     * 
     * @param parent
     *            the parent composite
     */
    private Button buildIncludeDerivedFilesButton(final Composite parent) {
        Button button = newCheckButton(parent, StringKeys.PROPERTY_BUTTON_INCLUDE_DERIVED_FILES);
        button.setSelection(model.isIncludeDerivedFiles());
        return button;
    }

    /**
     * Create the violations as errors checkbox.
     * 
     * @param parent
     *            the parent composite
     */
    private Button buildViolationsAsErrorsButton(final Composite parent) {
        Button button = newCheckButton(parent, StringKeys.PROPERTY_BUTTON_VIOLATIONS_AS_ERRORS);
        button.setSelection(model.violationsAsErrors());
        return button;
    }

    /**
     * Create the Run At Full Build.
     * 
     * @param parent
     *            the parent composite
     */
    private Button buildFullBuildEnabledButton(final Composite parent) {
        Button button = newCheckButton(parent, StringKeys.PROPERTY_BUTTON_RUN_AT_FULL_BUILD);
        button.setSelection(model.isFullBuildEnabled());
        return button;
    }

    /**
     * Create the radio button for storing configuration in a project file.
     * 
     * @param parent
     *            the parent composite
     */
    private Button buildStoreRuleSetInProjectButton(final Composite parent) {
        Button button = newRadioButton(parent, StringKeys.PROPERTY_BUTTON_STORE_RULESET_PROJECT);

        button.setSelection(model.isRuleSetStoredInProject());

        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                adjustControls();
            }
        });

        return button;
    }

    /**
     * Create the radio button for storing configuration in a project file
     * 
     * @param parent
     *            the parent composite
     */
    private Button buildLocalRulesButton(final Composite parent) {
        Button button = newRadioButton(parent, "Use local rules");

        button.setSelection(!model.isRuleSetStoredInProject());

        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                adjustControls();
            }
        });

        return button;
    }

    /**
     * Create the the rule set file name text.
     * 
     * @param parent
     *            the parent composite
     */
    private Text buildRuleSetFileText(Composite parent) {
        Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
        String ruleSetFile = model.getRuleSetFile();
        if (ruleSetFile != null) {
            text.setText(ruleSetFile);
        }
        activeControls.add(text);
        return text;
    }

    /**
     * Create the button for browsing for a ruleset file.
     * 
     * @param parent
     *            the parent composite
     */
    private Button buildRuleSetBrowseButton(final Composite parent) {
        Button button = newPushButton(parent, StringKeys.PROPERTY_BUTTON_RULESET_BROWSE);

        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO EMF's ResourceDialog would be better.
                FileDialog fileDialog = new FileDialog(getShell(), SWT.MULTI);
                String path = fileDialog.open();
                List<String> files = new ArrayList<>();
                String[] names = fileDialog.getFileNames();
                for (int i = 0, n = names.length; i < n; i++) {
                    StringBuffer buf = new StringBuffer(fileDialog.getFilterPath());
                    if (buf.charAt(buf.length() - 1) != File.separatorChar) {
                        buf.append(File.separatorChar);
                    }
                    buf.append(names[i]);
                    files.add(buf.toString());
                }
                if (path != null) {
                    ruleSetFileText.setText(StringUtils.join(files, ","));
                }
            }
        });

        return button;
    }

    /**
     * Build a label
     */
    private Label buildSelectedWorkingSetLabel(final Composite parent) {
        selectedWorkingSet = model.getProjectWorkingSet();

        final Label label = new Label(parent, SWT.NONE);
        label.setText(this.selectedWorkingSet == null ? getMessage(StringKeys.PROPERTY_LABEL_NO_WORKINGSET)
                : getMessage(StringKeys.PROPERTY_LABEL_SELECTED_WORKINGSET) + selectedWorkingSet.getName());
        activeControls.add(label);
        return label;
    }

    /**
     * Build rule table viewer
     */
    private Table buildAvailableRulesTableViewer(final Composite parent) {
        final int tableStyle = SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.CHECK;
        availableRulesTableViewer = new TableViewer(parent, tableStyle);

        final Table ruleTable = availableRulesTableViewer.getTable();

        addColumnTo(ruleTable, SWT.LEFT, true, StringKeys.PREF_RULESET_COLUMN_LANGUAGE, 70 + 20,
                RuleTableViewerSorter.RULE_LANGUAGE_COMPARATOR);
        addColumnTo(ruleTable, SWT.LEFT, true, StringKeys.PREF_RULESET_COLUMN_RULESET_NAME, 110,
                RuleTableViewerSorter.RULE_RULESET_NAME_COMPARATOR);
        addColumnTo(ruleTable, SWT.LEFT, true, StringKeys.PREF_RULESET_COLUMN_RULE_NAME, 170,
                RuleTableViewerSorter.RULE_NAME_COMPARATOR);
        // addColumnTo(ruleTable, SWT.LEFT, false, StringKeys.PREF_RULESET_COLUMN_SINCE, 40,
        // RuleTableViewerSorter.RULE_SINCE_COMPARATOR);
        addColumnTo(ruleTable, SWT.LEFT, false, StringKeys.PREF_RULESET_COLUMN_PRIORITY, 80,
                RuleTableViewerSorter.RULE_PRIORITY_COMPARATOR);
        // addColumnTo(ruleTable, SWT.LEFT, true, StringKeys.PREF_RULESET_COLUMN_DESCRIPTION, 300,
        // RuleTableViewerSorter.RULE_DESCRIPTION_COMPARATOR);

        ruleTable.setLinesVisible(true);
        ruleTable.setHeaderVisible(true);

        availableRulesTableViewer.setContentProvider(new RuleSetContentProvider());
        availableRulesTableViewer.setLabelProvider(new RuleLabelProvider());
        availableRulesTableViewer.setComparator(availableRuleTableViewerSorter);
        availableRulesTableViewer.setColumnProperties(new String[] { PROPERTY_LANGUAGE,
            PROPERTY_RULESET_NAME, PROPERTY_RULE_NAME,
            // PROPERTY_SINCE,
            PROPERTY_PRIORITY,
            // PROPERTY_DESCRIPTION
        });

        availableRulesTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateDescriptionField();
            }
        });

        populateAvailableRulesTable();

        activeControls.add(ruleTable);
        return ruleTable;
    }

    private void updateDescriptionField() {
        IStructuredSelection sel = (IStructuredSelection) availableRulesTableViewer.getSelection();
        Rule rule = (Rule) sel.getFirstElement();
        ruleDescriptionField.showFor(rule);
    }

    /**
     * Helper method to add new table columns.
     */
    private void addColumnTo(Table table, int alignment, boolean resizable, String textKey, int width,
            final Comparator<Rule> comparator) {

        TableColumn newColumn = new TableColumn(table, alignment);
        newColumn.setResizable(resizable);
        newColumn.setText(getMessage(textKey));
        newColumn.setWidth(width);
        if (comparator != null) {
            newColumn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    availableRuleTableViewerSorter.setComparator(comparator);
                    refresh();
                }
            });
        }
    }

    private Button newPushButton(Composite parent, String labelKey) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(getMessage(labelKey));
        activeControls.add(button);
        return button;
    }

    /**
     * Build the working set selection button.
     * 
     * @param parent
     */
    private void buildSelectWorkingSetButton(final Composite parent) {
        final Button workingSetButton = newPushButton(parent, StringKeys.PROPERTY_BUTTON_SELECT_WORKINGSET);
        workingSetButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectWorkingSet();
            }
        });
    }

    /**
     * Build the working set deselect button.
     * 
     * @param parent
     */
    private Button buildDeselectWorkingSetButton(final Composite parent) {
        final Button button = newPushButton(parent, StringKeys.PROPERTY_BUTTON_DESELECT_WORKINGSET);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deselectWorkingSet();
            }
        });

        return button;
    }

    /**
     * Populate the rule table.
     */
    private void populateAvailableRulesTable() {
        availableRulesTableViewer.setInput(controller.getAvailableRules());
        final List<RuleSet> activeRuleSet = model.getProjectRuleSetList();
        if (activeRuleSet != null) {
            final Collection<Rule> activeRules = InternalRuleSetUtil.allRules(activeRuleSet);

            final TableItem[] itemList = availableRulesTableViewer.getTable().getItems();
            for (TableItem element2 : itemList) {
                final Object rule = element2.getData();
                if (activeRules.contains(rule)) {
                    element2.setChecked(true);
                }
            }
        }
    }

    /**
     * User press OK Button.
     */
    @Override
    public boolean performOk() {
        LOG.info("Properties editing accepted");
        model.setPmdEnabled(enablePMDButton.getSelection());
        model.setProjectWorkingSet(selectedWorkingSet);
        model.setProjectRuleSet(getProjectRuleSet());
        model.setRuleSetStoredInProject(ruleSetStoredInProjectButton.getSelection());
        model.setRuleSetFile(ruleSetFileText.getText());
        model.setIncludeDerivedFiles(includeDerivedFilesButton.getSelection());
        model.setViolationsAsErrors(violationsAsErrorsButton.getSelection());
        model.setFullBuildEnabled(fullBuildEnabledButton.getSelection());

        return controller.performOk();
    }

    @Override
    public boolean performCancel() {
        LOG.info("Properties editing canceled");
        return super.performCancel();
    }

    /**
     * @return a RuleSet object from the selected table item
     */
    private RuleSet getProjectRuleSet() {
        RuleSet ruleSet = RuleSetUtil.newEmpty(RuleSetUtil.DEFAULT_RULESET_NAME,
                RuleSetUtil.DEFAULT_RULESET_DESCRIPTION);
        final TableItem[] rulesList = this.availableRulesTableViewer.getTable().getItems();

        for (TableItem element2 : rulesList) {
            if (element2.getChecked()) {
                final Rule rule = (Rule) element2.getData();
                ruleSet = RuleSetUtil.addRule(ruleSet, rule);
                // log.debug("Adding rule " + rule.getName() + " in the project ruleset");
            }
        }

        final List<RuleSet> activeRuleSet = model.getProjectRuleSetList();
        for (RuleSet rs : activeRuleSet) {
            ruleSet = InternalRuleSetUtil.addFileExclusions(ruleSet, rs.getFileExclusions());
            ruleSet = InternalRuleSetUtil.addFileInclusions(ruleSet, rs.getFileInclusions());
        }

        return ruleSet;
    }

    /**
     * Help user to select a working set for PMD.
     */
    protected void selectWorkingSet() {
        LOG.info("Select working set");
        this.setSelectedWorkingSet(this.controller.selectWorkingSet(this.selectedWorkingSet));
    }

    /**
     * Help user to deselect a working set for PMD.
     */
    protected void deselectWorkingSet() {
        LOG.info("Deselect working set");
        setSelectedWorkingSet(null);
    }

    /**
     * Process the change of the selected working set.
     * 
     * @param a
     *            newly selected working set
     */
    private void setSelectedWorkingSet(final IWorkingSet workingSet) {
        selectedWorkingSet = workingSet;
        selectedWorkingSetLabel.setText(selectedWorkingSet == null ? getMessage(StringKeys.PROPERTY_LABEL_NO_WORKINGSET)
                : getMessage(StringKeys.PROPERTY_LABEL_SELECTED_WORKINGSET) + selectedWorkingSet.getName());
        deselectWorkingSetButton.setEnabled(selectedWorkingSet != null);
    }

    /**
     * Helper method to shorten message access.
     * 
     * @param key
     *            a message key
     * @return requested message
     */
    private String getMessage(final String key) {
        return PMDPlugin.getDefault().getStringTable().getString(key);
    }

    /**
     * Refresh the list.
     */
    protected void refresh() {
        try {
            availableRulesTableViewer.getControl().setRedraw(false);
            // Preserve the checked rules across a refresh. Checked rules seem to be cleared when table is sorted.
            Collection<Rule> rules = getProjectRuleSet().getRules();
            availableRulesTableViewer.refresh();
            TableItem[] items = availableRulesTableViewer.getTable().getItems();
            for (TableItem item : items) {
                if (rules.contains(item.getData())) {
                    item.setChecked(true);
                }
            }
        } catch (ClassCastException e) {
            PMDPlugin.getDefault().logError("Ignoring exception while refreshing table", e);
        } finally {
            availableRulesTableViewer.getControl().setRedraw(true);
        }
    }

    private void enableExternalRulesetControls(boolean flag) {
        ruleSetBrowseButton.setEnabled(flag);
        ruleSetFileText.setEnabled(flag);
        showDescriptionField(!flag);
    }

    /**
     * Refresh based up whether using rule set in project or not.
     */
    protected void refreshRuleSetInProject() {
        Table ruleTable = availableRulesTableViewer.getTable();
        if (ruleSetStoredInProjectButton.getSelection()) {
            ruleTable.setEnabled(false);
            showDescriptionField(false);
            enableExternalRulesetControls(true);
        } else {
            ruleTable.setEnabled(true);
            showDescriptionField(true);
            enableExternalRulesetControls(false);
        }
    }
}
