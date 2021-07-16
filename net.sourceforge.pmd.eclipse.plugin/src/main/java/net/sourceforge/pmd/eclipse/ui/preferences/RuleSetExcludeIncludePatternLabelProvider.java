/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences;

/**
 * Implements a label provider for the item of the ruleset exclude/include
 * pattern tables of the PMD Preference page.
 * 
 */
public class RuleSetExcludeIncludePatternLabelProvider extends AbstractTableLabelProvider {

    @Override
    public String getColumnText(Object element, int columnIndex) {
        String result = "";
        if (element instanceof RuleSetExcludeIncludePattern) {
            RuleSetExcludeIncludePattern pattern = (RuleSetExcludeIncludePattern) element;
            if (columnIndex == 0) {
                return pattern.getPattern();
            }
        }
        return result;
    }
}
