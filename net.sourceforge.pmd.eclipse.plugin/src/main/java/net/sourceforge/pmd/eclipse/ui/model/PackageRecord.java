/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 * AbstractPMDRecord for a Package creates Files when instantiated
 *
 * @author SebastianRaffel ( 16.05.2005 ), Philippe Herlin, Sven Jacob
 *
 */
public class PackageRecord extends AbstractPMDRecord {
    private final IPackageFragment packageFragment;
    private final ProjectRecord parent;
    private AbstractPMDRecord[] children;

    /**
     * Constructor
     *
     * @param fragment,
     *            the PackageFragment
     * @param record,
     *            the Project
     */
    public PackageRecord(IPackageFragment fragment, ProjectRecord record) {
        super();

        if (fragment == null) {
            throw new IllegalArgumentException("fragment cannot be null");
        }

        if (record == null) {
            throw new IllegalArgumentException("record cannot be null");
        }

        this.packageFragment = fragment;
        this.parent = record;
        this.children = createChildren();
    }

    @Override
    public AbstractPMDRecord getParent() {
        return this.parent;
    }

    @Override
    public AbstractPMDRecord[] getChildren() {
        return children; // NOPMD by Herlin on 09/10/06 00:22
    }

    @Override
    public IResource getResource() {
        IResource resource = null;
        try {
            resource = packageFragment.getCorrespondingResource();
        } catch (JavaModelException jme) {
            PMDPlugin.getDefault().logError(StringKeys.ERROR_JAVAMODEL_EXCEPTION + this.toString(), jme);
        }
        return resource;
    }

    /**
     * Gets the Package's Fragment.
     *
     * @return the Fragment
     */
    public IPackageFragment getFragment() {
        return packageFragment;
    }

    @Override
    protected final AbstractPMDRecord[] createChildren() {
        List<FileRecord> fileList = new ArrayList<FileRecord>();
        try {
            ICompilationUnit[] javaUnits = packageFragment.getCompilationUnits();
            for (ICompilationUnit javaUnit : javaUnits) {
                IResource javaResource = javaUnit.getCorrespondingResource();
                if (javaResource != null) {
                    fileList.add(new FileRecord(javaResource, this)); // NOPMD
                    // by
                    // Herlin
                    // on
                    // 09/10/06
                    // 00:25
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

                children = new AbstractPMDRecord[files.size()]; // NOPMD
                // by
                // Herlin
                // on
                // 09/10/06
                // 00:31
                files.toArray(this.children);
                removed = true;
                removedFile = file;
            }
        }

        return removedFile;
    }

    @Override
    public String getName() {
        String name = packageFragment.getElementName();

        // for the default Package we return a String saying "default Package"
        if (packageFragment.isDefaultPackage()) {
            name = PMDPlugin.getDefault().getStringTable().getString(StringKeys.VIEW_DEFAULT_PACKAGE);
        }

        return name;
    }

    @Override
    public int getResourceType() {
        return TYPE_PACKAGE;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PackageRecord && packageFragment.equals(((PackageRecord) obj).packageFragment);
    }

    @Override
    public int hashCode() {
        return packageFragment.hashCode();
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
