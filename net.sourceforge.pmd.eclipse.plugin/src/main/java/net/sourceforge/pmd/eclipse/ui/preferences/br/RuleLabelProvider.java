/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.Image;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.eclipse.ui.preferences.AbstractTableLabelProvider;

/**
 *
 * @author Brian Remedios
 */
public class RuleLabelProvider extends AbstractTableLabelProvider {

    private RuleColumnDescriptor[] columnDescriptors;

    /**
     * Constructor for RuleLabelProvider.
     * 
     * @param columns
     *            RuleColumnDescriptor[]
     */
    public RuleLabelProvider(RuleColumnDescriptor[] columns) {
        columnDescriptors = columns;
    }

    public String getDetailText(Object element, int columnIndex) {

        if (columnIndex <= 0) {
            return "";
        }

        if (element instanceof Rule) {
            Rule rule = (Rule) element;
            String problem = rule.dysfunctionReason();
            if (StringUtils.isNotBlank(problem)) {
                return "Problem in " + rule.getName() + " rule: " + problem;
            }
            return columnDescriptors[columnIndex - 1].detailStringFor(rule);
        }

        if (element instanceof RuleGroup) {
            RuleGroup group = (RuleGroup) element;
            return columnDescriptors[columnIndex - 1].detailStringFor(group);
        }

        return "??";
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {

        if (columnIndex <= 0) {
            return "";
        }

        if (element instanceof RuleCollection) {
            if (columnIndex == 1) {
                RuleGroup rg = (RuleGroup) element;
                String label = rg.label();
                return standardized(label, rg.ruleCount());
            }
            return columnDescriptors[columnIndex - 1].stringValueFor((RuleCollection) element);
        }

        if (element instanceof Rule) {
            RuleColumnDescriptor rcd = columnDescriptors[columnIndex - 1];
            String text = rcd.stringValueFor((Rule) element);
            return columnIndex == 1 ? "   " + text : text;
        }

        return "??";
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {

        if (columnIndex <= 0) {
            return null;
        }

        if (element instanceof RuleCollection) {
            return columnDescriptors[columnIndex - 1].imageFor((RuleCollection) element);
        }

        if (element instanceof Rule) {
            return columnDescriptors[columnIndex - 1].imageFor((Rule) element);
        }

        return null; // should never get here
    }

    private String standardized(String rawLabel, int count) {

        int rulesPos = rawLabel.indexOf(" Rules");
        String filteredLabel = rulesPos > 0 ? rawLabel.substring(0, rulesPos) : rawLabel;

        return filteredLabel + "  (" + count + ")";
    }
}
