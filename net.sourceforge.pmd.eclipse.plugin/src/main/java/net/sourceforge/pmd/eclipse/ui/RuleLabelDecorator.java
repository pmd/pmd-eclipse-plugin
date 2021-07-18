/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.builder.MarkerUtil;
import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptorCache;
import net.sourceforge.pmd.eclipse.ui.views.PriorityFilter;

/**
 * 
 * @author Brian Remedios
 */
public class RuleLabelDecorator implements ILightweightLabelDecorator {
    public static final String ID = "net.sourceforge.pmd.eclipse.plugin.RuleLabelDecorator";

    private Set<ILabelProviderListener> listeners = new CopyOnWriteArraySet<>();

    @Override
    public void addListener(ILabelProviderListener listener) {
        listeners.add(listener);
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    public void changed(Collection<IResource> resources) {
        LabelProviderChangedEvent lpce = new LabelProviderChangedEvent(this, resources.toArray());

        for (ILabelProviderListener listener : listeners) {
            listener.labelProviderChanged(lpce);
        }
    }

    /**
     * @deprecated reloading is not necessary anymore
     */
    @Deprecated
    public void reloadDecorators() {
        // nothing to do
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void decorate(Object element, IDecoration decoration) {
        if (!(element instanceof IResource)) {
            return;
        }

        IResource resource = (IResource) element;

        Set<Integer> range = null;
        try {
            range = MarkerUtil.priorityRangeOf(resource, PMDRuntimeConstants.RULE_MARKER_TYPES, 5);
        } catch (CoreException e1) {
            return;
        }

        if (range.isEmpty()) {
            return;
        }

        // consider only the priorities, that are not filtered
        Integer highestPriority = null;
        for (Integer priority : range) {
            if (PriorityFilter.getInstance().isPriorityEnabled(RulePriority.valueOf(priority))) {
                if (highestPriority == null || highestPriority > priority) {
                    highestPriority = priority;
                }
            }
        }

        if (highestPriority == null) {
            return;
        }

        ImageDescriptor overlay = PriorityDescriptorCache.INSTANCE.descriptorFor(RulePriority.valueOf(highestPriority)).getAnnotationImageDescriptor();

        try {
            boolean hasMarkers = MarkerUtil.hasAnyRuleMarkers(resource);
            if (hasMarkers) {
                decoration.addOverlay(overlay);
            }
        } catch (CoreException e) {
            PMDPlugin.getDefault().logError("Error while adding overlay icon", e);
        }
    }
}
