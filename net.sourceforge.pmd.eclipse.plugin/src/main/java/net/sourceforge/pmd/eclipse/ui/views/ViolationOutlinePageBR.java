package net.sourceforge.pmd.eclipse.ui.views;

import java.util.Arrays;
import java.util.List;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.ItemColumnDescriptor;
import net.sourceforge.pmd.eclipse.ui.model.FileRecord;
import net.sourceforge.pmd.eclipse.ui.preferences.br.BasicTableManager;
import net.sourceforge.pmd.eclipse.ui.preferences.br.MarkerColumnsUI;
import net.sourceforge.pmd.eclipse.ui.views.actions.RemoveViolationAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.Page;

/**
 * Creates a Page for the Violation Outline
 * 
 * @author Brian Remedios
 */
public class ViolationOutlinePageBR extends Page implements IPage, ISelectionChangedListener, RefreshableTablePage {

    private TableViewer					tableViewer;
    private ViolationOutline			violationOutline;
    private ViewerFilter				viewerFilter;
    private FileRecord					resource;
    private Integer[]                   columnWidths;
    private Integer[]                   sorterProperties;
    private BasicTableManager<IMarker>	tableManager;

	private ItemColumnDescriptor<?,IMarker>[] initialColumns = new ItemColumnDescriptor[] {
		MarkerColumnsUI.priority,
		MarkerColumnsUI.lineNumber,
//		MarkerColumnsUI.done,
		MarkerColumnsUI.created,
		MarkerColumnsUI.ruleName,
		MarkerColumnsUI.message
		};
	
    /**
     * Constructor
     * 
     * @param resourceRecord, the FileRecord
     * @param outline, the parent Outline
     */
    public ViolationOutlinePageBR(FileRecord resourceRecord, ViolationOutline outline) {

        resource = resourceRecord;
        violationOutline = outline;

        ViewerFilter[] filters = outline.getFilters();
        for (int i = 0; i < filters.length; i++) {
            if (filters[i] instanceof PriorityFilter)
                viewerFilter = filters[i];
        }
    }

    public TableViewer tableViewer() { return tableViewer; }
    
    public void createControl(Composite parent) {
    	
    	tableManager = new BasicTableManager<IMarker>("rscViolations", PMDPlugin.getDefault().loadPreferences(), initialColumns);
        tableViewer = tableManager.buildTableViewer(parent);

        tableManager.setupColumns(initialColumns);
        tableManager.setTableMenu(violationOutline.createContextMenu(tableViewer));

        columnWidths = new Integer[initialColumns.length];
        for (int i = 0; i < initialColumns.length; i++) {
            columnWidths[i] = initialColumns[i].defaultWidth();
            final int columnIndex = i;
            tableViewer.getTable().getColumn(i).addControlListener(new ControlAdapter() {
                @Override
                public void controlResized(ControlEvent e) {
                    columnWidths[columnIndex] = tableViewer.getTable().getColumn(columnIndex).getWidth();
                }
            });
            tableViewer.getTable().getColumn(i).addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    sorterProperties[0] = columnIndex;
                    sorterProperties[1] = tableManager.getSortDirection();
                }
            });
        }
        sorterProperties = new Integer[2];
        sorterProperties[0] = null;
        sorterProperties[1] = SWT.NONE;

        // create the Table
        createActionBars();
        
        // set the Input
        tableViewer.setContentProvider(new ViolationOutlineContentProvider(this));

        tableViewer.setInput(resource);

        // add the Filter and Listener
        tableViewer.addFilter(viewerFilter);
        tableViewer.addSelectionChangedListener(this);
    }

    /**
     * Creates the ActionBars
     */
    private void createActionBars() {
        IToolBarManager manager = getSite().getActionBars().getToolBarManager();

        Action removeViolationAction = new RemoveViolationAction(tableViewer);
        manager.add(removeViolationAction);
        manager.add(new Separator());
    }

    /**
     * @return the Viewer
     */
    public TableViewer getTableViewer() {
        return tableViewer;
    }

    /* @see org.eclipse.ui.part.IPage#getControl() */
    public Control getControl() {
        return tableViewer.getControl();
    }

    /* @see org.eclipse.ui.part.IPage#setFocus() */
    public void setFocus() {
        tableViewer.getTable().setFocus();
    }

    /**
     * @return the underlying FileRecord
     */
    public FileRecord getResource() {
        return resource;
    }

    /**
     * Refreshes the View
     */
    public void refresh() {
        if (!tableViewer.getControl().isDisposed()) {
            tableViewer.getControl().setRedraw(false);
            tableViewer.refresh();
            tableViewer.getControl().setRedraw(true);
        }

    }

    /* @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent) */
    public void selectionChanged(SelectionChangedEvent event) {
    	
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        IMarker marker = (IMarker) selection.getFirstElement();
        if (marker == null) return;
            
        IEditorPart editor = getSite().getPage().getActiveEditor();
        if (editor == null) return;
            
        IEditorInput input = editor.getEditorInput();
        if (input instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput) input).getFile();
            if (marker.getResource().equals(file)) {
               IDE.gotoMarker(editor, marker);
               }
            }
    }

    public List<Integer> getColumnWidths() {
        return Arrays.asList(columnWidths);
    }

    public void setColumnWidths(List<Integer> widths) {
        if (widths == null || widths.isEmpty())
            return;
        if (tableViewer.getTable().isDisposed())
            return;

        columnWidths = widths.toArray(new Integer[widths.size()]);
        TableColumn[] columns = tableViewer.getTable().getColumns();
        for (int i = 0; i < columns.length && i < widths.size() && i < initialColumns.length; i++) {
            int width = initialColumns[i].defaultWidth();
            if (widths.get(i) != null) {
                width = widths.get(i).intValue();
            }
            columns[i].setWidth(width);
        }
    }

    /**
     * first: column index
     * second: ascending/descending: UP, DOWN or NONE
     */
    public List<Integer> getSorterProperties() {
        return Arrays.asList(sorterProperties);
    }

    public void setSorterProperties(List<Integer> sorterProps) {
        if (sorterProps == null || sorterProps.isEmpty())
            return;

        Table table = tableViewer.getTable();
        if (table.isDisposed()) {
            return;
        }

        sorterProperties = sorterProps.toArray(new Integer[sorterProps.size()]);
        TableColumn sortColumn = null;
        int direction = SWT.NONE;
        if (sorterProps.size() == 2) {
            if (sorterProps.get(0) != null && sorterProps.get(0).intValue() >= 0 && sorterProps.get(0).intValue() < table.getColumnCount()) {
                sortColumn = table.getColumn(sorterProps.get(0).intValue());
            }
            if (sorterProps.get(1) != null) {
                direction = sorterProps.get(1).intValue();
            }
        }
        if (sortColumn != null) {
            tableManager.setSortDirection(sortColumn, initialColumns[sorterProperties[0]].getAccessor(), direction);
        }
    }
}
