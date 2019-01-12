/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.preferences.impl;

import java.util.Set;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences;

/**
 * Stores preferences to restore UI state, such as selected row in the rule table.
 * TODO - replace this with the existing ViewMemento
 *
 * @author Brian Remedios
 */
public class PreferenceUIStore {

    public static final PreferenceUIStore INSTANCE = new PreferenceUIStore();

    private IPreferences preferences;

    private PreferenceUIStore() {
        initialize();
    }

    private void initialize() {
        preferences = PMDPlugin.getDefault().loadPreferences();
    }

    public void save() {
        preferences.sync();
    }

    public int tableFraction() {
        return preferences.tableFraction();
    }

    public void tableFraction(int aFraction) {
        preferences.tableFraction(aFraction);
    }

    public Set<String> hiddenColumnIds() {
        return preferences.getHiddenColumnIds();
    }

    public void hiddenColumnIds(Set<String> names) {
        preferences.setHiddenColumnIds(names);
    }

    public int selectedPropertyTab() {
        return preferences.getSelectedPropertyTab();
    }

    public void selectedPropertyTab(int anIndex) {
        preferences.setSelectedPropertyTab(anIndex);
    }

    public boolean globalRuleManagement() {
        return preferences.getGlobalRuleManagement();
    }

    public void globalRuleManagement(boolean b) {
        preferences.setGlobalRuleManagement(b);
    }

    public Set<String> selectedRuleNames() {
        return preferences.getSelectedRuleNames();
    }

    public void selectedRuleNames(Set<String> ruleNames) {
        preferences.setSelectedRuleNames(ruleNames);
    }

    public boolean sortDirectionUp() {
        return preferences.isSortDirectionUp();
    }

    public void sortDirectionUp(boolean isUp) {
        preferences.setSortDirectionUp(isUp);
    }

    public String groupingColumnName() {
        return preferences.getGroupingColumn();
    }

    public void groupingColumnName(String columnName) {
        preferences.setGroupingColumn(columnName);
    }
}
