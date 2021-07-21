/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;

import net.sourceforge.pmd.eclipse.ui.preferences.AbstractTableLabelProvider;

/**
 * 
 * @author Brian Remedios
 */
public class BasicTableLabelProvider extends AbstractTableLabelProvider {

    private final ItemColumnDescriptor<?, ?>[] columns;
    private final List<Image> imagesToBeDisposed = new ArrayList<>();

    public BasicTableLabelProvider(ItemColumnDescriptor<?, ?>[] theColumns) {
        columns = theColumns;
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public Image getColumnImage(Object element, int columnIndex) {
        ItemColumnDescriptor itemColumnDescriptor = columns[columnIndex];
        Image image = itemColumnDescriptor.imageFor(element);
        if (image != null && itemColumnDescriptor.shouldImageBeDisposed()) {
            imagesToBeDisposed.add(image);
        }
        return image;
    }

    public String getColumnText(Object element, int columnIndex) {
        ItemColumnDescriptor itemColumnDescriptor = columns[columnIndex];
        Object value = itemColumnDescriptor.textFor(element);
        return value == null ? null : value.toString();
    }

    public void addColumnsTo(Table table) {
        for (ItemColumnDescriptor<?, ?> desc : columns) {
            desc.buildTableColumn(table);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Image image : imagesToBeDisposed) {
            image.dispose();
        }
        imagesToBeDisposed.clear();
    }
}
