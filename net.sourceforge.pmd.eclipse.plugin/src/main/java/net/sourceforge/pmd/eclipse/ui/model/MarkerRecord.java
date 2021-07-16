/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

/**
 *
 * @author Sven
 *
 */

public class MarkerRecord extends AbstractPMDRecord {
    private AbstractPMDRecord[] children;
    private final FileRecord parent;
    private final String ruleName;
    private final int priority;
    private final List<IMarker> markers;

    /**
     * Constructor.
     *
     * @param javaResource the given File
     */
    public MarkerRecord(FileRecord parent, String ruleName, int priority) {
        super();
        this.parent = parent;
        this.ruleName = ruleName;
        this.priority = priority;
        this.markers = new ArrayList<>();
        this.children = EMPTY_RECORDS;
    }

    public void addViolation(IMarker marker) {
        this.markers.add(marker);
    }

    public int getViolationsCounted() {
        return markers.size();
    }

    @Override
    public AbstractPMDRecord addResource(IResource resource) {
        return null;
    }

    public void updateChildren() {
        this.children = createChildren();
    }

    @Override
    public final AbstractPMDRecord[] createChildren() {
        final List<AbstractPMDRecord> children = new ArrayList<>();

        final List<AbstractPMDRecord> markers = parent.getParent().findResourcesByName(this.ruleName, TYPE_MARKER);
        final Iterator<AbstractPMDRecord> markerIterator = markers.iterator();

        while (markerIterator.hasNext()) {
            final MarkerRecord marker = (MarkerRecord) markerIterator.next();
            children.add(new FileToMarkerRecord(marker)); // NOPMD by Sven on 13.11.06 12:05
        }

        return children.toArray(new AbstractPMDRecord[0]);
    }

    @Override
    public AbstractPMDRecord[] getChildren() {
        return children; // NOPMD by Sven on 13.11.06 12:05
    }

    @Override
    public String getName() {
        return ruleName;
    }

    public int getPriority() {
        return priority;
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
        return TYPE_MARKER;
    }

    @Override
    public AbstractPMDRecord removeResource(IResource resource) {
        return null;
    }

    @Override
    public boolean hasMarkers() {
        return !markers.isEmpty();
    }

    @Override
    public IMarker[] findMarkers() {
        return markers.toArray(new IMarker[0]);
    }

    @Override
    public int getNumberOfViolationsToPriority(int prio, boolean invertMarkerAndFileRecords) {
        int number = 0;
        if (prio == priority) {
            if (invertMarkerAndFileRecords) {
                for (AbstractPMDRecord element : children) {
                    number += element.getNumberOfViolationsToPriority(prio, false);
                }
            } else {
                number = getViolationsCounted();
            }
        }
        return number;
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
