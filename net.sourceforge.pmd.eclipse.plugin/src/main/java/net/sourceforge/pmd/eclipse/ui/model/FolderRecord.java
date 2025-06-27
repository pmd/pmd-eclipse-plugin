/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

public class FolderRecord extends AbstractPMDRecord {
    private final IFolder folder;
    private final ProjectRecord parent;
    private AbstractPMDRecord[] children;

    public FolderRecord(IFolder folder, ProjectRecord record) {
        super();

        if (folder == null) {
            throw new IllegalArgumentException("folder cannot be null");
        }

        if (record == null) {
            throw new IllegalArgumentException("record cannot be null");
        }

        this.folder = folder;
        this.parent = record;
        this.children = createChildren();
    }

    @Override
    public AbstractPMDRecord getParent() {
        return this.parent;
    }

    @Override
    public AbstractPMDRecord[] getChildren() {
        return children;
    }

    @Override
    public IResource getResource() {
        return (IResource) folder;
    }

    /**
     * Gets the Package's Fragment.
     *
     * @return the Fragment
     */
    public IFolder getFolder() {
        return folder;
    }

    @Override
    protected final AbstractPMDRecord[] createChildren() {
        List<FileRecord> fileList = new ArrayList<>();
        try {
            for (IResource member : folder.members()) {
                if (member != null) {
                    fileList.add(new FileRecord(member, this));
                }
            }
        } catch (CoreException ce) {
            PMDPlugin.getDefault().logError(StringKeys.ERROR_CORE_EXCEPTION + this.toString(), ce);
        }

        return fileList.toArray(new AbstractPMDRecord[0]);
    }

    @Override
    public AbstractPMDRecord addResource(IResource resource) {
        // final ICompilationUnit unit =
        // this.packageFragment.getCompilationUnit(resource.getName());
        FileRecord file;

        // TODO This should be more question of whether PMD is interested in the
        // File!
        // we want the File to be a java-File
        // if (unit != null) {
        // we create a new FileRecord and add it to the List
        file = new FileRecord(resource, this);
        final List<AbstractPMDRecord> files = getChildrenAsList();
        files.add(file);

        children = new AbstractPMDRecord[files.size()];
        files.toArray(this.children);
        // }

        return file;
    }

    @Override
    public AbstractPMDRecord removeResource(IResource resource) {
        final List<AbstractPMDRecord> files = getChildrenAsList();
        AbstractPMDRecord removedFile = null;
        boolean removed = false;

        for (int i = 0; i < files.size() && !removed; i++) {
            final AbstractPMDRecord file = files.get(i);

            // if the file is in here, remove it
            if (file.getResource().equals(resource)) {
                files.remove(i);

                children = new AbstractPMDRecord[files.size()];
                files.toArray(this.children);
                removed = true;
                removedFile = file;
            }
        }

        return removedFile;
    }

    @Override
    public String getName() {
        return folder.getName();
    }

    @Override
    public int getResourceType() {
        return TYPE_PACKAGE;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FolderRecord && folder.equals(((FolderRecord) obj).folder);
    }

    @Override
    public int hashCode() {
        return folder.hashCode();
    }

    @Override
    public int getNumberOfViolationsToPriority(int prio, boolean invertMarkerAndFileRecords) {
        int number = 0;
        for (AbstractPMDRecord element : children) {
            number += element.getNumberOfViolationsToPriority(prio, false);
        }

        return number;
    }

    @Override
    public int getLOC() {
        int number = 0;
        for (AbstractPMDRecord element : children) {
            number += element.getLOC();
        }

        return number;
    }

    @Override
    public int getNumberOfMethods() {
        int number = 0;
        for (AbstractPMDRecord element : children) {
            number += element.getNumberOfMethods();
        }

        return number;
    }
}
