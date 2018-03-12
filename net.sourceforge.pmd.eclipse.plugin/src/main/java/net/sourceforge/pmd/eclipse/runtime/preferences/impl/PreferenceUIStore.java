
package net.sourceforge.pmd.eclipse.runtime.preferences.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.PreferenceStore;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.preferences.br.RuleColumnDescriptor;
import net.sourceforge.pmd.eclipse.ui.preferences.br.RuleTableColumns;
import net.sourceforge.pmd.eclipse.ui.preferences.editors.SWTUtil;

/**
 * 
 * 
 * @author Brian Remedios
 */
public class PreferenceUIStore {

    private PreferenceStore preferenceStore;

    private static final String TABLE_FRACTION = PMDPlugin.PLUGIN_ID + ".ruletable.fraction";
    private static final String TABLE_HIDDEN_COLS = PMDPlugin.PLUGIN_ID + ".ruletable.hiddenColumns";
    private static final String TABLE_COLUMN_SORT_UP = PMDPlugin.PLUGIN_ID + ".ruletable.sortUp";
    private static final String GROUPING_COLUMN = PMDPlugin.PLUGIN_ID + ".ruletable.groupingColumn";
    private static final String SELECTED_RULE_NAMES = PMDPlugin.PLUGIN_ID + ".ruletable.selectedRules";
    private static final String SELECTED_PROPERTY_TAB = PMDPlugin.PLUGIN_ID + ".ruletable.selectedPropertyTab";
    private static final String GLOBAL_RULE_MANAGEMENT = PMDPlugin.PLUGIN_ID + ".globalRuleManagement";

    private static final int TABLE_FRACTION_DEFAULT = 55;
    private static final char STRING_SEPARATOR = ',';

    private static final RuleColumnDescriptor[] DEFAULT_HIDDEN_COLUMNS = new RuleColumnDescriptor[] {
        RuleTableColumns.externalURL, RuleTableColumns.minLangVers, RuleTableColumns.fixCount,
        RuleTableColumns.exampleCount, RuleTableColumns.maxLangVers, RuleTableColumns.since,
        RuleTableColumns.modCount };

    private static final boolean DEFAULT_SORT_UP = false;

    public static final PreferenceUIStore INSTANCE = new PreferenceUIStore();

    private PreferenceUIStore() {
        initialize();
    }

    private static String defaultHiddenColumnIds() {
        Set<String> colNames = new HashSet<String>(DEFAULT_HIDDEN_COLUMNS.length);
        for (RuleColumnDescriptor rcDesc : DEFAULT_HIDDEN_COLUMNS) {
            colNames.add(rcDesc.id());
        }
        return SWTUtil.asString(colNames, STRING_SEPARATOR);
    }

    private void initialize() {

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IPath path = root.getLocation();
        String fileName = path.append(PreferencesManagerImpl.NEW_PREFERENCE_LOCATION).toString();

        // TODO - replace this with the existing ViewMemento
        preferenceStore = new PreferenceStore(fileName);
        preferenceStore.setDefault(GLOBAL_RULE_MANAGEMENT, false);

        try {
            preferenceStore.load();
        } catch (IOException e) {
            createNewStore();
        }
    }

    private void createNewStore() {

        preferenceStore.setValue(TABLE_FRACTION, TABLE_FRACTION_DEFAULT);
        preferenceStore.setValue(TABLE_HIDDEN_COLS, defaultHiddenColumnIds());
        preferenceStore.setValue(TABLE_COLUMN_SORT_UP, DEFAULT_SORT_UP);
        preferenceStore.setValue(GROUPING_COLUMN, "");
        preferenceStore.setValue(SELECTED_RULE_NAMES, "");
        preferenceStore.setValue(SELECTED_PROPERTY_TAB, 0);
        preferenceStore.setValue(GLOBAL_RULE_MANAGEMENT, false);

        save();
    }

    public void save() {

        try {
            preferenceStore.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int tableFraction() {
        return preferenceStore.getInt(TABLE_FRACTION);
    }

    public void tableFraction(int aFraction) {
        preferenceStore.setValue(TABLE_FRACTION, aFraction);
    }

    public Set<String> hiddenColumnIds() {
        String names = preferenceStore.getString(TABLE_HIDDEN_COLS);
        return SWTUtil.asStringSet(names, STRING_SEPARATOR);
    }

    public void hiddenColumnIds(Set<String> names) {
        String nameStr = SWTUtil.asString(names, STRING_SEPARATOR);
        preferenceStore.setValue(TABLE_HIDDEN_COLS, nameStr);
    }

    public int selectedPropertyTab() {
        return preferenceStore.getInt(SELECTED_PROPERTY_TAB);
    }

    public void selectedPropertyTab(int anIndex) {
        preferenceStore.setValue(SELECTED_PROPERTY_TAB, anIndex);
    }

    public boolean globalRuleManagement() {
        return preferenceStore.getBoolean(GLOBAL_RULE_MANAGEMENT);
    }

    public void globalRuleManagement(boolean b) {
        preferenceStore.setValue(GLOBAL_RULE_MANAGEMENT, b);
    }

    public Set<String> selectedRuleNames() {
        String names = preferenceStore.getString(SELECTED_RULE_NAMES);
        return SWTUtil.asStringSet(names, STRING_SEPARATOR);
    }

    public void selectedRuleNames(Collection<String> ruleNames) {
        String nameStr = SWTUtil.asString(ruleNames, STRING_SEPARATOR);
        preferenceStore.setValue(SELECTED_RULE_NAMES, nameStr);
    }

    public boolean sortDirectionUp() {
        return preferenceStore.getBoolean(TABLE_COLUMN_SORT_UP);
    }

    public void sortDirectionUp(boolean isUp) {
        preferenceStore.setValue(TABLE_COLUMN_SORT_UP, isUp);
    }

    public String groupingColumnName() {
        return preferenceStore.getString(GROUPING_COLUMN);
    }

    public void groupingColumnName(String columnName) {
        preferenceStore.setValue(GROUPING_COLUMN, columnName);
    }
}
