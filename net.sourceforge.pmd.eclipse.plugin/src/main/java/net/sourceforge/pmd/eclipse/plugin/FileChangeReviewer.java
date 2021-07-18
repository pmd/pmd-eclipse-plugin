/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.plugin;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.runtime.cmd.ReviewCodeCmd;

/**
 * Monitors for changes in the workspace and initiates the ReviewCodeCmd when
 * suitable file changes in some meaningful way.
 * 
 * @author Brian Remedios
 */
public class FileChangeReviewer implements IResourceChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(FileChangeReviewer.class);

    private static boolean autoBuildingHintLogged;

    private enum ChangeType {
        ADDED, REMOVED, CHANGED
    }

    private class ResourceChange {
        public final ChangeType resourceDeltaType;
        public final int flags;
        public final IFile file;

        private ResourceChange(ChangeType type, IFile theFile, int theFlags) {
            resourceDeltaType = type;
            file = theFile;
            flags = theFlags;
        }

        @Override
        public int hashCode() {
            return resourceDeltaType.hashCode() + 13 + file.hashCode() + flags;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (other == this) {
                return true;
            }
            if (other.getClass() == getClass()) {
                ResourceChange chg = (ResourceChange) other;
                return chg.file.equals(file) && resourceDeltaType == chg.resourceDeltaType && flags == chg.flags;
            }
            return false;
        }
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        IWorkspaceDescription workspaceSettings = ResourcesPlugin.getWorkspace().getDescription();
        if (workspaceSettings.isAutoBuilding()) {
            if (!autoBuildingHintLogged) {
                autoBuildingHintLogged = true;
                LOG.info("Not running PMD via FileChangeReviewer, as autoBuilding is enabled for this workspace.");
            }
            return;
        }

        // reset this flag, so the next time, the log shows again.
        autoBuildingHintLogged = false;

        Set<ResourceChange> itemsChanged = new HashSet<>();

        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            changed(itemsChanged, event.getDelta(), new NullProgressMonitor());
        }

        if (itemsChanged.isEmpty()) {
            return;
        }

        ReviewCodeCmd cmd = new ReviewCodeCmd(); // separate one for each thread
        cmd.reset();

        for (ResourceChange chg : itemsChanged) {
            cmd.addResource(chg.file);
        }

        try {
            cmd.performExecute();
        } catch (RuntimeException e) {
            LOG.error("Error processing code review upon file changes: {}", e.toString(), e);
        }
    }

    private void changed(Set<ResourceChange> itemsChanged, IResourceDelta delta, IProgressMonitor monitor) {

        IResource rsc = delta.getResource();
        int flags = delta.getFlags();

        switch (delta.getKind()) {
        case IResourceDelta.NO_CHANGE:
            return;
        case IResourceDelta.REMOVED:
            // if (rsc instanceof IProject) {
            // removed(itemsChanged, (IProject)rsc, delta.getFlags());
            // }
            // if (rsc instanceof IFile) {
            // removed(itemsChanged, (IFile)rsc, flags, true);
            // }
            for (IResourceDelta grandkidDelta : delta.getAffectedChildren()) {
                if (monitor.isCanceled()) {
                    return;
                }
                changed(itemsChanged, grandkidDelta, monitor);
            }
            break;
        case IResourceDelta.ADDED:
            // if (rsc instanceof IProject) {
            // removed(itemsChanged, (IProject)rsc, delta.getFlags());
            // }
            if (rsc instanceof IFile) {
                added(itemsChanged, (IFile) rsc, flags);
            }
            for (IResourceDelta grandkidDelta : delta.getAffectedChildren()) {
                if (monitor.isCanceled()) {
                    return;
                }
                changed(itemsChanged, grandkidDelta, monitor);
            }
            break;
        case IResourceDelta.CHANGED:
            // if (rsc instanceof IProject) {
            // changed(itemsChanged, (IProject)rsc, delta.getFlags());
            // }
            if (rsc instanceof IFile) {
                changed(itemsChanged, (IFile) rsc, flags);
            }
            for (IResourceDelta grandkidDelta : delta.getAffectedChildren()) {
                if (monitor.isCanceled()) {
                    return;
                }
                changed(itemsChanged, grandkidDelta, monitor);
            }
            break;
        default:
            for (IResourceDelta grandkidDelta : delta.getAffectedChildren()) {
                if (monitor.isCanceled()) {
                    return;
                }
                changed(itemsChanged, grandkidDelta, monitor);
            }
        }
    }

    private void changed(Set<ResourceChange> itemsChanged, IFile rsc, int flags) {

        if ((flags & IResourceDelta.CONTENT) > 0) {
            itemsChanged.add(new ResourceChange(ChangeType.CHANGED, rsc, flags));
        }
    }

    private void added(Set<ResourceChange> itemsChanged, IFile rsc, int flags) {
        itemsChanged.add(new ResourceChange(ChangeType.ADDED, rsc, flags));
        // System.out.println("added: " + rsc);
    }
    // private void changed(Set<ResourceChange> itemsChanged, IProject rsc, int
    // flags) {
    // System.out.println("changed: " + rsc);
    // }
    //
    //
    // private void removed(Set<ResourceChange> itemsChanged, IFile rsc, int
    // flags, boolean b) {
    // itemsChanged.add( new ResourceChange(ChangeType.REMOVED, rsc, flags));
    // System.out.println("removed: " + rsc);
    // }
    //
    // private void removed(Set<ResourceChange> itemsChanged, IProject rsc, int
    // flags) {
    // System.out.println("removed: " + rsc);
    // }

}
