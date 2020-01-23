/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows the modifications of the element of the rule properties
 * table of the PMD Preference page
 * 
 * @author Philippe Herlin
 *
 */
public class RulePropertyCellModifier implements ICellModifier {
    private static final Logger LOG = LoggerFactory.getLogger(RulePropertyCellModifier.class);
    private TableViewer tableViewer;

    /**
     * Constructor
     */
    public RulePropertyCellModifier(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    /**
     * @see org.eclipse.jface.viewers.ICellModifier#canModify(Object, String)
     */
    public boolean canModify(Object element, String property) {
        return property.equalsIgnoreCase(PMDPreferencePage.PROPERTY_VALUE);
    }

    /**
     * @see org.eclipse.jface.viewers.ICellModifier#getValue(Object, String)
     */
    public Object getValue(Object element, String property) {
        Object result = null;
        if (element instanceof RuleProperty) {
            RuleProperty ruleProperty = (RuleProperty) element;
            if (property.equalsIgnoreCase(PMDPreferencePage.PROPERTY_PROPERTY)) {
                result = ruleProperty.getProperty();
                LOG.debug("Interrogation de la propriété : " + result);
            } else if (property.equalsIgnoreCase(PMDPreferencePage.PROPERTY_VALUE)) {
                result = ruleProperty.getValue();
                LOG.debug("Interrogation de la valeur de la propriété : " + result);
            }
        }
        return result;
    }

    /**
     * @see org.eclipse.jface.viewers.ICellModifier#modify(Object, String,
     *      Object)
     */
    public void modify(Object element, String property, Object value) {
        TableItem item = (TableItem) element;

        if (item.getData() instanceof RuleProperty) {
            RuleProperty ruleProperty = (RuleProperty) item.getData();
            if (property.equalsIgnoreCase(PMDPreferencePage.PROPERTY_VALUE)) {
                ruleProperty.setValue((String) value);
                tableViewer.update(ruleProperty, new String[] { PMDPreferencePage.PROPERTY_VALUE });
                PMDPreferencePage.getActiveInstance().setModified(true);
                LOG.debug("modification de la valeur de la propriété : " + value);
            }
        }
    }

}
