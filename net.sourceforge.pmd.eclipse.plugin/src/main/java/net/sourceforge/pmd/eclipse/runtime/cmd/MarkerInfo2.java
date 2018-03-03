
package net.sourceforge.pmd.eclipse.runtime.cmd;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 * @author Brian Remedios
 */
public class MarkerInfo2 {

    private final String type;
    private Map<String, Object> data;

    public MarkerInfo2(String theType, int expectedSize) {
        type = theType;
        data = new HashMap<String, Object>(expectedSize);
    }

    public void add(String name, Object value) {
        data.put(name, value);
    }

    public void add(String name, int value) {
        add(name, Integer.valueOf(value));
    }

    public void addAsMarkerTo(IFile file) throws CoreException {

        IMarker marker = file.createMarker(type);
        marker.setAttributes(data.keySet().toArray(new String[data.size()]), data.values().toArray());
    }

    public String toString() {
        return "MarkerInfo2: rule=" + data.get("rulename") + ", message=" + data.get(IMarker.MESSAGE) + ", line="
                + data.get(IMarker.LINE_NUMBER);
    }
}
