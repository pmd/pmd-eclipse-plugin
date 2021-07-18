/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import net.sourceforge.pmd.eclipse.runtime.builder.MarkerUtil;
import net.sourceforge.pmd.eclipse.ui.model.FileRecord;
import net.sourceforge.pmd.eclipse.util.Util;

/**
 * Provides the ViolationOutlinePages with Content.
 *
 * @author SebastianRaffel ( 08.05.2005 )
 */
public class ViolationOutlineContentProvider implements IStructuredContentProvider, IResourceChangeListener {

    private RefreshableTablePage tablePage;
    private TableViewer tableViewer;
    private FileRecord resource;

    public ViolationOutlineContentProvider(RefreshableTablePage page) {
        tablePage = page;
        tableViewer = page.tableViewer();
    }

    @Override
    public Object[] getElements(Object inputElement) {

        if (inputElement instanceof FileRecord) {
            return ((FileRecord) inputElement).findMarkers();
        }
        return Util.EMPTY_ARRAY;
    }

    @Override
    public void dispose() {
        // TODO
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (resource != null) {
            resource.getResource().getWorkspace().removeResourceChangeListener(this);
        }

        // we create a new FileRecord
        resource = (FileRecord) newInput;
        if (resource != null) {
            resource.getResource().getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        }
        tableViewer = (TableViewer) viewer;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (resource == null || !resource.getResource().exists()) {
            return;
        }

        List<IMarkerDelta> markerDeltas = MarkerUtil.markerDeltasIn(event);

        if (markerDeltas.isEmpty()) {
            return;
        }

        // we search for removed, added or changed Markers
        final List<IMarker> additions = new ArrayList<>();
        final List<IMarker> removals = new ArrayList<>();
        final List<IMarker> changes = new ArrayList<>();

        for (IMarkerDelta delta : markerDeltas) {
            if (!delta.getResource().equals(resource.getResource())) {
                continue;
            }
            IMarker marker = delta.getMarker();
            switch (delta.getKind()) {
            case IResourceDelta.ADDED:
                additions.add(marker);
                break;
            case IResourceDelta.REMOVED:
                removals.add(marker);
                break;
            case IResourceDelta.CHANGED:
                changes.add(marker);
                break;
            default:
                //TODO: do we need to handle other changes?
                break;
            }
        }

        // updating the table MUST be in sync
        tableViewer.getControl().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                updateViewer(additions, removals, changes);
            }
        });
    }

    /**
     * Applies found updates on the table, adapted from Philippe Herlin.
     *
     * @param additions
     * @param removals
     * @param changes
     */
    protected void updateViewer(List<IMarker> additions, List<IMarker> removals, List<IMarker> changes) {
        // perform removals
        if (!removals.isEmpty()) {
            tableViewer.cancelEditing();
            tableViewer.remove(removals.toArray());
        }

        // perform additions
        if (!additions.isEmpty()) {
            tableViewer.add(additions.toArray());
        }

        // perform changes
        if (!changes.isEmpty()) {
            tableViewer.update(changes.toArray(), null);
        }

        tablePage.refresh();
    }
}
