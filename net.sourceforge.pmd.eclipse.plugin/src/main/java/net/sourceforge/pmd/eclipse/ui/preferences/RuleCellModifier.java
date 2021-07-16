/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RulePriority;

/**
 * Implements a cell modifier for rule properties editing in the rule table
 * of the PMD Preference page
 * 
 * @author Philippe Herlin
 * @deprecated
 */
@Deprecated
public class RuleCellModifier implements ICellModifier {
    private TableViewer tableViewer;

    /**
     * Constructor
     */
    public RuleCellModifier(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    @Override
    public boolean canModify(Object element, String property) {
        return property.equalsIgnoreCase(PMDPreferencePage.PROPERTY_PRIORITY)
            || property.equalsIgnoreCase(PMDPreferencePage.PROPERTY_DESCRIPTION);
    }

    @Override
    public Object getValue(Object element, String property) {
        Object result = null;

        if (element instanceof Rule) {
            Rule rule = (Rule) element;
            if (property.equalsIgnoreCase(PMDPreferencePage.PROPERTY_LANGUAGE)) {
                result = rule.getLanguage().getShortName();
            } else if (property.equalsIgnoreCase(PMDPreferencePage.PROPERTY_RULESET_NAME)) {
                result = rule.getRuleSetName();
            } else if (property.equalsIgnoreCase(PMDPreferencePage.PROPERTY_RULE_NAME)) {
                result = rule.getName();
            } else if (property.equalsIgnoreCase(PMDPreferencePage.PROPERTY_PRIORITY)) {
                result = Integer.valueOf(rule.getPriority().getPriority() - 1);
            } else if (property.equalsIgnoreCase(PMDPreferencePage.PROPERTY_DESCRIPTION)) {
                result = rule.getDescription();
            }
        }

        return result;
    }

    @Override
    public void modify(Object element, String property, Object value) {
        TableItem item = (TableItem) element;

        if (item.getData() instanceof Rule) {
            Rule rule = (Rule) item.getData();
            if (property.equalsIgnoreCase(PMDPreferencePage.PROPERTY_PRIORITY)) {
                rule.setPriority(RulePriority.valueOf(((Integer) value).intValue() + 1));
                PMDPreferencePage.getActiveInstance().setModified(true);
                //tableViewer.update(rule, new String[] { "priority" });
                tableViewer.refresh();
            } else if (property.equalsIgnoreCase(PMDPreferencePage.PROPERTY_DESCRIPTION)) {
                rule.setDescription((String) value);
                PMDPreferencePage.getActiveInstance().setModified(true);
                //tableViewer.update(rule, new String[] { "description" });
                tableViewer.refresh();
            }
        }
    }

}
