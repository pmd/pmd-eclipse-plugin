/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptorCache;
import net.sourceforge.pmd.lang.rule.RulePriority;

/**
 * Provides the ViolationsOutlinePages with labels and images.
 * 
 * @author SebastianRaffel ( 08.05.2005 )
 */
public class ViolationOutlineLabelProvider extends LabelProvider implements ITableLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        IMarker marker;
        if (element instanceof IMarker) {
            marker = (IMarker) element;
        } else {
            return null;
        }

        if (columnIndex == 0) {
            Integer priority = 0;
            try {
                priority = (Integer) marker.getAttribute(PMDUiConstants.KEY_MARKERATT_PRIORITY);
            } catch (CoreException ce) {
                PMDPlugin.getDefault().logError(StringKeys.ERROR_CORE_EXCEPTION + toString(), ce);
            }

            return PriorityDescriptorCache.INSTANCE.descriptorFor(RulePriority.valueOf(priority)).getAnnotationImage();
        }

        return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        IMarker marker;
        if (element instanceof IMarker) {
            marker = (IMarker) element;
        } else {
            return null;
        }

        switch (columnIndex) {
        // show the Message
        case 1:
            return marker.getAttribute(IMarker.MESSAGE, PMDUiConstants.KEY_MARKERATT_RULENAME);
        // show the Line
        case 2:
            return String.valueOf(marker.getAttribute(IMarker.LINE_NUMBER, 0));
        default:
            return "";
        }
    }
}
