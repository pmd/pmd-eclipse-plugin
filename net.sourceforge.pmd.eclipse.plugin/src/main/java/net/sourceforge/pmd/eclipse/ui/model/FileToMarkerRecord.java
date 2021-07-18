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

    @Override
    public AbstractPMDRecord addResource(IResource resource) {
        return null;
    }

    @Override
    protected AbstractPMDRecord[] createChildren() {
        return EMPTY_RECORDS;
    }

    @Override
    public AbstractPMDRecord[] getChildren() {
        return EMPTY_RECORDS;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public int getNumberOfViolationsToPriority(int prio, boolean invertMarkerAndFileRecords) {
        return parent.getNumberOfViolationsToPriority(prio, false);
    }

    @Override
    public AbstractPMDRecord getParent() {
        return parent;
    }

    @Override
    public IResource getResource() {
        return parent.getResource();
    }

    @Override
    public int getResourceType() {
        return 0;
    }

    @Override
    public AbstractPMDRecord removeResource(IResource resource) {
        return null;
    }

    @Override
    public boolean hasMarkers() {
        return parent.hasMarkers();
    }

    @Override
    public IMarker[] findMarkers() {
        return parent.findMarkers();
    }

    @Override
    public int getLOC() {
        return parent.getLOC();
    }

    @Override
    public int getNumberOfMethods() {
        return parent.getNumberOfMethods();
    }
}
