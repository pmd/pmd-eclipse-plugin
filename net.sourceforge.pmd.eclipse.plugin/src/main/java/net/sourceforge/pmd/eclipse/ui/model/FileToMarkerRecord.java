/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

/**
 * 
 * @author Sven
 *
 */

public class FileToMarkerRecord extends AbstractPMDRecord {
    private final MarkerRecord parent;
    
    public FileToMarkerRecord(MarkerRecord parent) {
        super();
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord#addResource(org.eclipse.core.resources.IResource)
     */
    public AbstractPMDRecord addResource(IResource resource) {
        return null;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord#createChildren()
     */
    protected AbstractPMDRecord[] createChildren() {
        return EMPTY_RECORDS;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord#getChildren()
     */
    public AbstractPMDRecord[] getChildren() {
        return EMPTY_RECORDS;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord#getName()
     */
    public String getName() {
        return "";
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord#getNumberOfViolationsToPriority(int, boolean)
     */
    public int getNumberOfViolationsToPriority(int prio, boolean invertMarkerAndFileRecords) {
        return parent.getNumberOfViolationsToPriority(prio, false);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord#getParent()
     */
    public AbstractPMDRecord getParent() {
        return parent;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord#getResource()
     */
    public IResource getResource() {
        return parent.getResource();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord#getResourceType()
     */
    public int getResourceType() {
        return 0;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord#removeResource(org.eclipse.core.resources.IResource)
     */
    public AbstractPMDRecord removeResource(IResource resource) {
        return null;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord#hasMarkers()
     */
    public boolean hasMarkers() {
        return parent.hasMarkers();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord#findMarkers()
     */
    public IMarker[] findMarkers() {
        return parent.findMarkers();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord#getLOC()
     */
    public int getLOC() {
        return parent.getLOC();
    }

    /* (non-Javadoc)
     * @see net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord#getNumberOfMethods()
     */
    public int getNumberOfMethods() {
        return parent.getNumberOfMethods();
    }
}
