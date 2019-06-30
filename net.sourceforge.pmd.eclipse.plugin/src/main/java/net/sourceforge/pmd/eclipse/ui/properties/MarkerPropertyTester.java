/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.properties;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

public class MarkerPropertyTester extends PropertyTester {
    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (!(receiver instanceof IMarker)) {
            return false;
        }

        IMarker marker = (IMarker) receiver;

        try {
            return marker.getType().startsWith(PMDPlugin.PLUGIN_ID);
        } catch (CoreException e) {
            PMDPlugin.getDefault().logError(e.getMessage(), e);
        }
        return false;
    }

}
