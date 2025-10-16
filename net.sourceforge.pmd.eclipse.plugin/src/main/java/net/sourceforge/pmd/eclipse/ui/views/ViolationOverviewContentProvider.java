/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord;
import net.sourceforge.pmd.eclipse.ui.model.FileRecord;
import net.sourceforge.pmd.eclipse.ui.model.FileToMarkerRecord;
import net.sourceforge.pmd.eclipse.ui.model.FolderRecord;
import net.sourceforge.pmd.eclipse.ui.model.MarkerRecord;
import net.sourceforge.pmd.eclipse.ui.model.PackageRecord;
import net.sourceforge.pmd.eclipse.ui.model.ProjectRecord;
import net.sourceforge.pmd.eclipse.ui.model.RootRecord;
import net.sourceforge.pmd.eclipse.util.Util;

/**
 * Provides the Violation Overview with Content Elements can be PackageRecords or FileRecords
 *
 * @author SebastianRaffel ( 09.05.2005 ), Philppe Herlin, Sven Jacob
 *
 */
public class ViolationOverviewContentProvider
        implements ITreeContentProvider, IStructuredContentProvider, IResourceChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(ViolationOverviewContentProvider.class);
    protected boolean filterPackages;

    private final ViolationOverview violationView;
    private TreeViewer treeViewer;

    private RootRecord root;
    private ChangeEvaluator changeEvaluator;

    /**
     * Constructor
     *
     * @param view
     */
    public ViolationOverviewContentProvider(ViolationOverview view) {
        super();

        violationView = view;
        treeViewer = view.getViewer();
    }

    @Override
    public void dispose() {
        if (root != null) {
            IWorkspaceRoot workspaceRoot = (IWorkspaceRoot) root.getResource();
            workspaceRoot.getWorkspace().removeResourceChangeListener(this);
        }
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof IWorkspaceRoot || parentElement instanceof RootRecord) {
            return getChildrenOfRoot();
        } else if (parentElement instanceof PackageRecord) {
            return getChildrenOfPackage((PackageRecord) parentElement);
        } else if (parentElement instanceof FolderRecord) {
            return getChildrenOfFolder((FolderRecord) parentElement);
        } else if (parentElement instanceof FileRecord) {
            return getChildrenOfFile((FileRecord) parentElement);
        } else if (parentElement instanceof MarkerRecord) {
            return getChildrenOfMarker((MarkerRecord) parentElement);
        }
        return Util.EMPTY_ARRAY;
    }

    /**
     * Gets the children of a file record.
     * 
     * @param record
     *            FileRecord
     * @return children as array
     */
    private Object[] getChildrenOfFile(FileRecord record) {
        return record.getChildren();
    }

    /**
     * Gets the children of a marker record.
     * 
     * @param record
     *            MarkerRecord
     * @return children as array
     */
    private Object[] getChildrenOfMarker(MarkerRecord record) {
        record.updateChildren();
        return record.getChildren();
    }

    /**
     * Gets the children of a PackageRecord. If the presentation type is {@link ViolationOverview#SHOW_MARKERS_FILES}
     * the children (MarkerRecord) of the children (FileRecord) will be get.
     *
     * @param record
     *            PackageRecord
     * @return children as array
     */
    private Object[] getChildrenOfPackage(PackageRecord record) {
        return getChildrenOfPackageOrFolder(record);
    }

    private Object[] getChildrenOfFolder(FolderRecord record) {
        return getChildrenOfPackageOrFolder(record);
    }

    private Object[] getChildrenOfPackageOrFolder(AbstractPMDRecord record) {
        if (violationView.getShowType() == ViolationOverview.SHOW_MARKERS_FILES) {
            Map<String, AbstractPMDRecord> markers = new HashMap<>();
            List<AbstractPMDRecord> files = record.getChildrenAsList();
            for (AbstractPMDRecord fileRec : files) {
                List<AbstractPMDRecord> newMarkers = fileRec.getChildrenAsList();
                for (AbstractPMDRecord markerRec : newMarkers) {
                    markers.put(markerRec.getName(), markerRec);
                }
            }

            return markers.values().toArray(new MarkerRecord[0]);
        } else {
            return record.getChildren();
        }
    }

    /**
     * Gets the children of the root depending on the show type.
     * 
     * @return children
     */
    private Object[] getChildrenOfRoot() {
        // ... we care about its Project's
        List<AbstractPMDRecord> projects = root.getChildrenAsList();
        ProjectRecord[] projectArray = new ProjectRecord[projects.size()];
        projects.toArray(projectArray);

        // we make a List of all Packages
        List<AbstractPMDRecord> packages = new ArrayList<>();
        for (ProjectRecord element : projectArray) {
            if (element.isProjectOpen()) {
                packages.addAll(element.getChildrenAsList());
            }
        }

        switch (violationView.getShowType()) {
        case ViolationOverview.SHOW_MARKERS_FILES:
        case ViolationOverview.SHOW_PACKAGES_FILES_MARKERS:
            // show packages
            return packages.toArray();

        case ViolationOverview.SHOW_FILES_MARKERS:
            // show files
            List<AbstractPMDRecord> files = new ArrayList<>();
            for (AbstractPMDRecord packageRec : packages) {
                files.addAll(packageRec.getChildrenAsList());
            }

            return files.toArray();

        default:
            // do nothing
        }
        return Util.EMPTY_ARRAY;
    }

    @Override
    public Object getParent(Object element) {
        Object parent = null;
        AbstractPMDRecord record = (AbstractPMDRecord) element;

        switch (violationView.getShowType()) {
        case ViolationOverview.SHOW_FILES_MARKERS:
            if (element instanceof FileRecord) {
                parent = root;
            } else {
                parent = record.getParent();
            }
            break;
        case ViolationOverview.SHOW_MARKERS_FILES:
            if (element instanceof FileToMarkerRecord) {
                parent = record.getParent();
            } else if (element instanceof PackageRecord) {
                parent = root;
            } else if (element instanceof MarkerRecord) {
                parent = record.getParent().getParent();
            }

            break;
        case ViolationOverview.SHOW_PACKAGES_FILES_MARKERS:
            if (element instanceof PackageRecord) {
                parent = root;
            } else {
                parent = record.getParent();
            }

            break;
        default:
            // do nothing
        }

        return parent;
    }

    @Override
    public boolean hasChildren(Object element) {
        boolean hasChildren = true;

        // find out if this is the last level in the tree (to avaoid recursion)
        switch (violationView.getShowType()) {
        case ViolationOverview.SHOW_PACKAGES_FILES_MARKERS:
        case ViolationOverview.SHOW_FILES_MARKERS:
            hasChildren ^= element instanceof MarkerRecord;
            break;
        case ViolationOverview.SHOW_MARKERS_FILES:
            hasChildren ^= element instanceof FileToMarkerRecord;
            break;
        default:
            // do nothing
        }

        if (hasChildren) {
            hasChildren = getChildren(element).length > 0;
        }
        return hasChildren;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        LOG.debug("ViolationOverview inputChanged");
        treeViewer = (TreeViewer) viewer;

        // this is called, when the View is instantiated and gets Input
        // or if the Source of Input changes

        // we remove an existing ResourceChangeListener
        IWorkspaceRoot workspaceRoot;
        if (root != null) {
            LOG.debug("remove current listener");
            workspaceRoot = (IWorkspaceRoot) root.getResource();
            workspaceRoot.getWorkspace().removeResourceChangeListener(this);
        }

        // ... to add a new one, so we can listen to Changes made
        // to Resources in the Workspace
        if (newInput instanceof IWorkspaceRoot) {
            LOG.debug("the new input is a workspace root");
            // either we got a WorkspaceRoot
            workspaceRoot = (IWorkspaceRoot) newInput;
            root = new RootRecord(workspaceRoot);
            workspaceRoot.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        } else if (newInput instanceof RootRecord) {
            LOG.debug("the new input is a root record");
            // ... or already a Record for it
            root = (RootRecord) newInput;
            workspaceRoot = (IWorkspaceRoot) root.getResource();
            workspaceRoot.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        }

        changeEvaluator = new ChangeEvaluator(root);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        final ChangeRecord<AbstractPMDRecord> changes = changeEvaluator.changeRecordFor(event);

        // the additions, removals and changes are given to the viewer so that it can update itself
        treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                updateViewer(changes);
            }
        });
    }

    protected void updateViewer(ChangeRecord<AbstractPMDRecord> changes) {

        // perform removals
        if (changes.hasRemovals()) {
            treeViewer.cancelEditing();
            treeViewer.remove(changes.removals.toArray());
        }

        // perform additions (if any)
        for (AbstractPMDRecord addedRec : changes.additions) {
            if (addedRec instanceof FileRecord) {
                treeViewer.add(addedRec.getParent(), addedRec);
            } else {
                treeViewer.add(root, addedRec);
            }
        }

        // perform changes
        if (changes.hasChanges()) {
            treeViewer.update(changes.changes.toArray(), null);
        }

        violationView.refresh();
    }
}
