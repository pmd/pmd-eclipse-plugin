package net.sourceforge.pmd.eclipse.ui.views;

import java.util.Arrays;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
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
    protected int 						currentSortedColumn;
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

    public Integer[] getColumnWidths() {
        TableColumn[] columns = tableViewer.getTable().getColumns();
        Integer[] result = new Integer[columns.length];
        for (int i = 0; i < columns.length; i++) {
            result[i] = columns[i].getWidth();
        }
        return result;
    }

    public void setColumnWidths(Integer[] widthArray) {
        TableColumn[] columns = tableViewer.getTable().getColumns();
        for (int i = 0; i < columns.length && i < widthArray.length && i < initialColumns.length; i++) {
            int width = initialColumns[i].defaultWidth();
            if (widthArray[i] != null) {
                width = widthArray[i].intValue();
            }
            columns[i].setWidth(width);
        }
    }

    /**
     * first: column index
     * second: ascending/descending: UP, DOWN or NONE
     */
    public Integer[] getSorterProperties() {
        Table table = tableViewer.getTable();
        int columnIndex = Arrays.asList(table.getColumns()).indexOf(table.getSortColumn());
        int direction = table.getSortDirection();
        return new Integer[]{columnIndex, direction};
    }

    public void setSorterProperties(Integer[] sorterProps) {
        Table table = tableViewer.getTable();
        TableColumn sortColumn = null;
        int direction = SWT.NONE;
        if (sorterProps.length == 2) {
            if (sorterProps[0] != null && sorterProps[0].intValue() < table.getColumnCount()) {
                sortColumn = table.getColumn(sorterProps[0].intValue());
            }
            if (sorterProps[1] != null) {
                direction = sorterProps[1].intValue();
            }
        }
        table.setSortColumn(sortColumn);
        table.setSortDirection(direction);
    }
}
