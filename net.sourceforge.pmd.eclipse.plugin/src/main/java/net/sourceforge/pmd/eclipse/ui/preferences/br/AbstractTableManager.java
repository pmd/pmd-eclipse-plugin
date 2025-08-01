/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences;
import net.sourceforge.pmd.eclipse.runtime.preferences.impl.PreferenceUIStore;
import net.sourceforge.pmd.eclipse.ui.ColumnDescriptor;

/**
 * Core table behaviour including menus and support for hiding/showing columns
 * 
 * @author Brian Remedios
 */
public abstract class AbstractTableManager<T extends Object> implements SortListener {

    private final String widgetId; // NOPMD unused // for saving preference values
    protected boolean sortDescending;
    protected TableColumn sortColumn;
    protected Object columnSorter; // cast to concrete type in subclass
    protected IPreferences preferences;
    protected Menu headerMenu;
    protected Menu tableMenu;

    // columns shown in the rule treetable in the desired order
    protected final ColumnDescriptor[] availableColumns; 

    private final Set<ColumnDescriptor> hiddenColumns = new HashSet<>();

    protected static PMDPlugin plugin = PMDPlugin.getDefault();

    public AbstractTableManager(String theWidgetId, IPreferences thePreferences, ColumnDescriptor[] theColumns) {
        super();

        widgetId = theWidgetId;
        preferences = thePreferences;
        availableColumns = theColumns;

        loadHiddenColumns();
    }

    protected static class WidthChangeThread extends Thread {
        private final int startWidth;
        private final int endWidth;
        private final ColumnWidthAdapter column;

        protected WidthChangeThread(int start, int end, ColumnWidthAdapter theColumn) {
            super();
            startWidth = start;
            endWidth = end;
            column = theColumn;
        }

        protected void setWidth(final int width) {
            column.display().syncExec(new Runnable() {
                @Override
                public void run() {
                    // delay(10);
                    column.width(width);
                }
            });
        }

        @Override
        public void run() {
            if (endWidth > startWidth) {
                for (int i = startWidth; i <= endWidth; i++) {
                    setWidth(i);
                }
            } else {
                for (int i = startWidth; i >= endWidth; i--) {
                    setWidth(i);
                }
            }
        }
    }

    // unifies table and tree column behaviour as one type
    protected interface ColumnWidthAdapter { 

        int width();

        void width(int newWidth);

        Display display();

        void setData(String key, Object value);

        Object getData(String key);
    }

    protected static ColumnWidthAdapter adapterFor(final TableColumn column) {
        return new ColumnWidthAdapter() {
            @Override
            public int width() {
                return column.getWidth();
            }

            @Override
            public void width(int newWidth) {
                column.setWidth(newWidth);
            }

            @Override
            public Display display() {
                return column.getDisplay();
            }

            @Override
            public void setData(String key, Object value) {
                column.setData(key, value);
            }

            @Override
            public Object getData(String key) {
                return column.getData(key);
            }
        };
    }

    protected abstract ColumnWidthAdapter columnAdapterFor(ColumnDescriptor desc);

    protected void setupMenusFor(final Control control) {

        final Display display = control.getDisplay();
        Shell shell = control.getShell();

        setupMenus(shell);

        control.addListener(SWT.MenuDetect, new Listener() {
            @Override
            public void handleEvent(Event event) {
                Point pt = display.map(null, control, new Point(event.x, event.y));
                Rectangle clientArea = clientAreaFor(control);
                boolean isHeader = clientArea.y <= pt.y && pt.y < (clientArea.y + headerHeightFor(control));
                if (!isHeader) {
                    adjustTableMenuOptions();
                }
                setMenu(control, isHeader ? headerMenu : tableMenu);
            }
        });

        addDisposeListener(control);
    }

    protected abstract Rectangle clientAreaFor(Control tableOrTreeControl);

    protected abstract int headerHeightFor(Control tableOrTreeControl);

    protected abstract void setMenu(Control contro, Menu menu);

    protected abstract void saveItemSelections();

    public void saveUIState() {
        saveItemSelections();
    }

    protected void setupMenus(Shell shell) {

        headerMenu = new Menu(shell, SWT.POP_UP);
        addHeaderSelectionOptions(headerMenu);

        tableMenu = new Menu(shell, SWT.POP_UP);
        addTableSelectionOptions(tableMenu);
    }

    public void setTableMenu(Menu tableMenu) {
        this.tableMenu = tableMenu;
    }

    protected void addDeleteListener(Control control) {

        control.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent ev) {
                if (ev.character == SWT.DEL) {
                    removeSelectedItems();
                }
            }
        });
    }

    protected void addHeaderSelectionOptions(Menu menu) {

        for (ColumnDescriptor desc : availableColumns) {
            MenuItem columnItem = new MenuItem(menu, SWT.CHECK);
            columnItem.setSelection(!isHidden(desc));
            columnItem.setText(desc.label());
            final ColumnDescriptor columnDesc = desc;
            columnItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    toggleColumnVisiblity(columnDesc);
                }
            });
        }
    }

    protected abstract void removeSelectedItems();

    protected void addTableSelectionOptions(Menu menu) {
        // subclasses to provide this
    }

    protected void adjustTableMenuOptions() {
        // subclasses to provide this
    }

    protected void addDisposeListener(Control control) {

        control.addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                headerMenu.dispose();
                tableMenu.dispose();
            }
        });
    }

    public void visible(ColumnDescriptor column, boolean show) {

        if (show) {
            show(column);
        } else {
            hide(column);
        }
    }

    protected void show(ColumnDescriptor desc) {
        hiddenColumns.remove(desc);

        ColumnWidthAdapter cwa = columnAdapterFor(desc);

        Object widthData = cwa.getData("restoredWidth");
        int width = widthData == null ? desc.defaultWidth() : ((Integer) widthData).intValue();
        WidthChangeThread t = new WidthChangeThread(0, width, cwa);
        t.start();
    }

    protected void hide(ColumnDescriptor desc) {
        hiddenColumns.add(desc);

        ColumnWidthAdapter cwa = columnAdapterFor(desc);

        cwa.setData("restoredWidth", Integer.valueOf(cwa.width()));
        WidthChangeThread t = new WidthChangeThread(cwa.width(), 0, cwa);
        t.start();
    }

    protected boolean isHidden(ColumnDescriptor desc) {
        return hiddenColumns.contains(desc);
    }

    protected boolean isActive(String item) {
        return preferences.isActive(item);
    }

    protected void isActive(String item, boolean flag) {
        preferences.isActive(item, flag);
    }

    protected void toggleColumnVisiblity(ColumnDescriptor desc) {

        if (hiddenColumns.contains(desc)) {
            show(desc);
        } else {
            hide(desc);
        }

        storeHiddenColumns();
        // redrawTable();
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    @Override
    public void sortBy(Object accessor, Object context) {
        if (columnSorter == accessor) {
            sortDescending = !sortDescending;
        } else {
            columnSorter = accessor;
        }

        sortColumn = (TableColumn) context;
        redrawTable(idFor(context), getSortDirection());
    }

    public int getSortDirection() {
        return sortDescending ? SWT.DOWN : SWT.UP;
    }

    public void setSortDirection(TableColumn column, Object accessor, int direction) {
        if (column != null && accessor != null) {
            sortDescending = direction == SWT.DOWN;
            sortColumn = column;
            columnSorter = accessor;
            redrawTable(idFor(sortColumn), direction);
        }
    }

    protected abstract String idFor(Object column);

    protected abstract void redrawTable(String columnId, int sortDirection);

    private void storeHiddenColumns() {
        Set<String> columnIds = new HashSet<>(hiddenColumns.size());
        for (ColumnDescriptor desc : hiddenColumns) {
            columnIds.add(desc.id());
        }

        PreferenceUIStore.INSTANCE.hiddenColumnIds(columnIds);
    }

    private void loadHiddenColumns() {

        for (String columnId : PreferenceUIStore.INSTANCE.hiddenColumnIds()) {
            for (ColumnDescriptor desc : availableColumns) {
                if (desc.id().equals(columnId)) {
                    hiddenColumns.add(desc);
                }
            }
        }
    }

    /**
     * Helper method to shorten message access
     * 
     * @param key
     *            a message key
     * @return requested message
     */
    protected String getMessage(String key) {
        return PMDPlugin.getDefault().getStringTable().getString(key);
    }
}
