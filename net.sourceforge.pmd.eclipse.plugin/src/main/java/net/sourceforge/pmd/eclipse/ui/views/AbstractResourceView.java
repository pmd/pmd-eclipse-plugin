/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.model.FileRecord;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 * 
 * @author Brian Remedios
 */
public abstract class AbstractResourceView extends AbstractPMDPagebookView implements IResourceChangeListener {

    protected AbstractResourceView() {
        // protected constructor for subclassing
    }

    protected static boolean getBoolUIPref(String prefId) {
        return pStore().getBoolean(prefId);
    }

    protected static IPreferenceStore pStore() {
        return PMDPlugin.getDefault().getPreferenceStore();
    }

    protected abstract AbstractStructureInspectorPage getCurrentViewPage();

    @Override
    public void partActivated(IWorkbenchPart part) {
        IWorkbenchPart activePart = getSitePage().getActivePart();
        if (activePart == null) {
            getSitePage().activate(this);
        }
        super.partActivated(part);
    }

    protected IPath getResourcePath() {
        AbstractStructureInspectorPage page = getCurrentViewPage();
        FileRecord record = page.getFileRecord();
        IResource resource = record.getResource();
        return resource.getFullPath();
    }

    protected void setUIPref(String prefId, boolean flag) {
        pStore().setValue(prefId, flag);
    }

    protected void setupListener(FileRecord resourceRecord) {
        resourceRecord.getResource().getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
        FileRecord resourceRecord = getFileRecordFromWorkbenchPart(part);
        if (resourceRecord != null) {
            resourceRecord.getResource().getWorkspace().removeResourceChangeListener(this);
        }

        AbstractStructureInspectorPage page = (AbstractStructureInspectorPage) pageRecord.page;

        if (page != null) {
            page.dispose();
        }

        pageRecord.dispose();
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        try {
            event.getDelta().accept(new IResourceDeltaVisitor() {
                @Override
                public boolean visit(final IResourceDelta delta) throws CoreException {
                    // find the resource for the path of the current page
                    IPath path = getResourcePath();
                    if (delta.getFullPath().equals(path)) {
                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                refresh(delta.getResource());
                            }
                        });

                        return false;
                    }
                    return true;
                }

            });
        } catch (CoreException e) {
            PMDPlugin.getDefault().logError(StringKeys.ERROR_CORE_EXCEPTION, e);
        }
    }

    /**
     * Refresh, reloads the View with a given new resource.
     * 
     * @param newResource
     *            new resource for the current active page.
     */
    protected void refresh(IResource newResource) {
        AbstractStructureInspectorPage page = getCurrentViewPage();
        if (page != null) {
            page.refresh(newResource);
        }
    }
}
