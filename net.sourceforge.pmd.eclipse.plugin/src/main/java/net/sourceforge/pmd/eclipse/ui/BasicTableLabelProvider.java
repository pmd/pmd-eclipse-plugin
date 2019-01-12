/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;

import net.sourceforge.pmd.eclipse.ui.preferences.AbstractTableLabelProvider;

/**
 * 
 * @author Brian Remedios
 */
public class BasicTableLabelProvider extends AbstractTableLabelProvider {

    private final ItemColumnDescriptor[] columns;

    public BasicTableLabelProvider(ItemColumnDescriptor[] theColumns) {
        columns = theColumns;
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public Image getColumnImage(Object element, int columnIndex) {

        return columns[columnIndex].imageFor(element);
    }

    public String getColumnText(Object element, int columnIndex) {

        Object value = columns[columnIndex].textFor(element);
        return value == null ? null : value.toString();
    }

    public void addColumnsTo(Table table) {

        for (ItemColumnDescriptor desc : columns) {
            desc.buildTableColumn(table);
        }
    }
}
