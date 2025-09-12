/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views;

import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Displays an Arrow-Image in a TableColumn, that shows in which Direction the Column is sorted.
 * 
 * @author SebastianRaffel ( 22.05.2005 ), Philippe Herlin, Brian Remedios
 */
public class TableColumnSorter extends ViewerSorter {

    /**
     * @param column,
     *            the column to sort
     * @param order,
     *            the Direction to sort by, -1 (desc) or 1 (asc)
     */
    public TableColumnSorter(TreeColumn column, int order) {
        super();

        // column.getParent().setSortColumn(column);
        // column.getParent().setSortDirection(order == 1 ? SWT.UP : SWT.DOWN);
    }

    /**
     * @param column,
     *            the column to sort
     * @param order,
     *            the Direction to sort by, -1 (desc) or 1 (asc)
     */
    public TableColumnSorter(TableColumn column, int order) {
        super();

        // column.getParent().setSortColumn(column);
        // column.getParent().setSortDirection(order == 1 ? SWT.UP : SWT.DOWN);
    }
}
