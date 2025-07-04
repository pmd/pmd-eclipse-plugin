/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

/**
 * Command to delete single markers.
 * This is useful if a large number of marker have to be deleted in order to run this in background.
 * For unknown reasons this took some time.
 *
 * @author Sven
 */
public class DeleteMarkersCommand extends AbstractDefaultCommand {

    private IMarker[] markers;

    public DeleteMarkersCommand() {
        super("DeleteMarkersCommand", "Deletes a possible large number of markers");

        setOutputProperties(true);
        setReadOnly(false);
        setTerminated(false);
        setMarkers(null);
        setUserInitiated(false);
    }

    public final void setMarkers(IMarker[] theMarkers) {
        markers = theMarkers;
    }

    @Override
    public boolean isReadyToExecute() {
        return markers != null;
    }

    @Override
    public void execute() {
        try {
            beginTask("Deleting single markers", markers.length);
            for (int j = 0; j < markers.length && !isCanceled(); j++) {
                markers[j].delete();
                worked(1);
            }
            done();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reset() {
        setMarkers(null);
    }

}
