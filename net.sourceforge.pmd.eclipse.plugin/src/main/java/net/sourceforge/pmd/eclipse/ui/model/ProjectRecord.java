/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 * AbstractPMDRecord for Projects creates Packages when instantiated
 *
 * @author SebastianRaffel ( 16.05.2005 ), Philippe Herlin, Sven Jacob
 *
 */
public class ProjectRecord extends AbstractPMDRecord {
    private final IProject project;
    private final RootRecord parent;
    private AbstractPMDRecord[] children;
    private boolean isJavaProject = false;

    /**
     * Constructor
     *
     * @param proj,
     *            the Project
     * @param record,
     *            the RootRecord
     */
    public ProjectRecord(IProject project, RootRecord record) {
        super();

        if (project == null) {
            throw new IllegalArgumentException("project cannot be null");
        }

        if (record == null) {
            throw new IllegalArgumentException("record cannot be null");

        }

        this.project = project;
        this.parent = record;

        try {
            isJavaProject = project.hasNature(JavaCore.NATURE_ID);
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (project.isAccessible()) {
            children = createChildren();
        } else {
            children = EMPTY_RECORDS;
        }

    }

    @Override
    public AbstractPMDRecord getParent() {
        return parent;
    }

    @Override
    public AbstractPMDRecord[] getChildren() {
        return children;
    }

    @Override
    public IResource getResource() {
        return project;
    }

    @Override
    protected final AbstractPMDRecord[] createChildren() {
        final Set<AbstractPMDRecord> packages = new HashSet<>();
        try {
            // search for Packages
            project.accept(new IResourceVisitor() {
                @Override
                public boolean visit(IResource resource) throws CoreException {
                    boolean visitChildren;
                    switch (resource.getType()) {
                    case IResource.FOLDER:
                        visitChildren = ProjectRecord.this.isJavaProject ? visitAsPackages(resource)
                                : visitAsFolders(resource);
                        break;
                    case IResource.PROJECT:
                        visitChildren = true;
                        break;
                    default:
                        visitChildren = false;
                        break;
                    }

                    return visitChildren;
                }

                private Boolean visitAsPackages(IResource resource) {
                    IJavaElement javaMember = JavaCore.create(resource);

                    if (javaMember == null) {
                        return true;
                    } else {
                        if (javaMember instanceof IPackageFragmentRoot) {
                            // if the Element is the Root of all Packages
                            // get all packages from it and add them to the
                            // list
                            // (e.g. for "org.eclipse.core.resources" and
                            // "org.eclipse.core" the root is
                            // "org.eclipse.core")
                            packages.addAll(createPackagesFromFragmentRoot((IPackageFragmentRoot) javaMember));
                        } else if (javaMember instanceof IPackageFragment
                                && javaMember.getParent() instanceof IPackageFragmentRoot) {
                            // if the Element is a Package get its Root and
                            // do the same as above
                            final IPackageFragment fragment = (IPackageFragment) javaMember;
                            packages.addAll(
                                    createPackagesFromFragmentRoot((IPackageFragmentRoot) fragment.getParent()));
                        }

                        return false;
                    }
                }

                private Boolean visitAsFolders(IResource resource) {
                    IFolder folder = (IFolder) resource;
                    packages.addAll(createPackagesFromFolderRoot(folder));
                    return false;
                }
            });
        } catch (CoreException ce) {
            PMDPlugin.getDefault().logError(StringKeys.ERROR_CORE_EXCEPTION + this.toString(), ce);
        }

        // return the List as an Array of Packages
        return packages.toArray(new AbstractPMDRecord[0]);
    }

    /**
     * Search for the Packages to a given FragmentRoot (Package-Root) and create
     * PackageRecords for them.
     *
     * @param root
     * @return
     */
    protected final Set<PackageRecord> createPackagesFromFragmentRoot(IPackageFragmentRoot root) {
        final Set<PackageRecord> packages = new HashSet<>();
        IJavaElement[] fragments = null;
        try {
            // search for all children
            fragments = root.getChildren();
            for (IJavaElement fragment : fragments) {
                if (fragment instanceof IPackageFragment) {
                    // create a PackageRecord for the Fragment
                    // and add it to the list
                    packages.add(new PackageRecord((IPackageFragment) fragment, this));
                }
            }
        } catch (JavaModelException jme) {
            PMDPlugin.getDefault().logError(StringKeys.ERROR_JAVAMODEL_EXCEPTION + this.toString(), jme);
        }

        return packages;
    }

    protected final Set<FolderRecord> createPackagesFromFolderRoot(IFolder rootFolder) {
        final Set<FolderRecord> folder = new HashSet<>();

        try {
            for (IResource resource : rootFolder.members()) {
                if (resource instanceof IFolder) {
                    folder.add(new FolderRecord((IFolder) resource, this));
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }

        return folder;
    }

    @Override
    public String getName() {
        return project.getName();
    }

    /**
     * Checks, if the underlying Project is open.
     *
     * @return true, if the Project is open, false otherwise
     */
    public boolean isProjectOpen() {
        return project.isOpen();
    }

    @Override
    public int getResourceType() {
        return TYPE_PROJECT;
    }

    @Override
    public AbstractPMDRecord addResource(IResource resource) {
        AbstractPMDRecord added = null;

        // we only care about Files
        if (resource instanceof IFile) {
            if (isJavaProject) {
                added = addToJavaProject(resource);
            } else {
                added = addToOtherProject(resource);
            }
        }

        return added;
    }

    @Override
    public AbstractPMDRecord removeResource(IResource resource) {
        AbstractPMDRecord removed = null;

        // we only care about Files
        if (resource instanceof IFile) {
            if (isJavaProject) {
                removed = removeFromJavaProject(resource);
            } else {
                removed = removeFromOtherProject(resource);
            }
        }

        return removed;
    }

    private AbstractPMDRecord removeFromJavaProject(IResource resource) {
        AbstractPMDRecord removedResource = null;

        IPackageFragment fragment;

        final IJavaElement element = JavaCore.create(resource.getParent());
        if (element instanceof IPackageFragment) {
            fragment = (IPackageFragment) element;
        } else {
            fragment = ((IPackageFragmentRoot) element).getPackageFragment("");
        }

        PackageRecord packageRec;

        // like above we compare Fragments to find the right Package
        for (int k = 0; k < children.length && removedResource == null; k++) {
            packageRec = (PackageRecord) children[k];
            if (packageRec.getFragment().equals(fragment)) {

                // if we found it, we remove the File
                final AbstractPMDRecord fileRec = packageRec.removeResource(resource);
                if (packageRec.getChildren().length == 0) {
                    // ... and if the Package is empty too
                    // we also remove it
                    final List<AbstractPMDRecord> packages = getChildrenAsList();
                    packages.remove(packageRec);

                    children = new AbstractPMDRecord[packages.size()];
                    packages.toArray(children);
                }

                removedResource = fileRec;
            }
        }

        return removedResource;
    }

    private AbstractPMDRecord removeFromOtherProject(IResource resource) {
        AbstractPMDRecord removedResource = null;

        IFolder folder = (IFolder) resource.getParent();
        FolderRecord folderRec;

        // like above we compare Fragments to find the right Package
        for (int k = 0; k < children.length && removedResource == null; k++) {
            folderRec = (FolderRecord) children[k];
            if (folderRec.getFolder().equals(folder)) {

                // if we found it, we remove the File
                final AbstractPMDRecord fileRec = folderRec.removeResource(resource);
                if (folderRec.getChildren().length == 0) {
                    // ... and if the Package is empty too
                    // we also remove it
                    final List<AbstractPMDRecord> packages = getChildrenAsList();
                    packages.remove(folderRec);

                    children = new AbstractPMDRecord[packages.size()];
                    packages.toArray(children);
                }

                removedResource = fileRec;
            }
        }

        return removedResource;
    }

    private AbstractPMDRecord addToJavaProject(IResource resource) {
        AbstractPMDRecord addedResource = null;

        IJavaElement javaMember = JavaCore.create(resource.getParent());
        if (javaMember instanceof IPackageFragmentRoot) {
            javaMember = ((IPackageFragmentRoot) javaMember).getPackageFragment("");
        }

        final IPackageFragment fragment = (IPackageFragment) javaMember;

        // we search int the children Packages for the File's Package
        // by comparing their Fragments
        for (int k = 0; k < children.length && addedResource == null; k++) {
            final PackageRecord packageRec = (PackageRecord) children[k];
            if (packageRec.getFragment().equals(fragment)) {
                // if the Package exists
                // we delegate to its addResource-function
                addedResource = packageRec.addResource(resource);
            }
        }

        // ... else we create a new Record for the new Package
        if (addedResource == null) {
            final PackageRecord packageRec = new PackageRecord(fragment, this);
            final List<AbstractPMDRecord> packages = getChildrenAsList();
            packages.add(packageRec);

            // ... and we add a new FileRecord to it
            children = new AbstractPMDRecord[packages.size()];
            packages.toArray(children);
            addedResource = packageRec.addResource(resource);
        }

        return addedResource;
    }

    private AbstractPMDRecord addToOtherProject(IResource resource) {
        AbstractPMDRecord addedResource = null;

        IFolder folder = (IFolder) resource.getParent();

        // we search int the children Packages for the File's Package
        // by comparing their Fragments
        for (int k = 0; k < children.length && addedResource == null; k++) {
            final FolderRecord folderRec = (FolderRecord) children[k];
            if (folderRec.getFolder().equals(folder)) {
                // if the Package exists
                // we delegate to its addResource-function
                addedResource = folderRec.addResource(resource);
            }
        }

        // ... else we create a new Record for the new Package
        if (addedResource == null) {
            final FolderRecord packageRec = new FolderRecord(folder, this);
            final List<AbstractPMDRecord> packages = getChildrenAsList();
            packages.add(packageRec);

            // ... and we add a new FileRecord to it
            children = new AbstractPMDRecord[packages.size()];
            packages.toArray(children);
            addedResource = packageRec.addResource(resource);
        }

        return addedResource;
    }

    @Override
    public int getNumberOfViolationsToPriority(int prio, boolean invertMarkerAndFileRecords) {
        int number = 0;
        for (AbstractPMDRecord element : children) {
            number += element.getNumberOfViolationsToPriority(prio, invertMarkerAndFileRecords);
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
