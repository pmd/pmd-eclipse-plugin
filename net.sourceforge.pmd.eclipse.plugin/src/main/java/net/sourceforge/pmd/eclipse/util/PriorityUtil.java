/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.util;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.eclipse.plugin.UISettings;
import net.sourceforge.pmd.eclipse.ui.views.PriorityFilter;

/**
 * Priority Util is a manager of the available priorities in the app. This Util will update if the Violations Overview
 * Priority Filter updates which will cause the File markers, Tree Markers, and Violation Outline markers to update.
 * 
 * @author Phillip Krall
 *
 * @deprecated use directly {@link PriorityFilter} instead.
 */
@Deprecated
public final class PriorityUtil {

    private PriorityUtil() {
    }

    /**
     * Check if a priority is turned on or not
     * 
     * @param priority
     * @return
     * @deprecated use {@link PriorityFilter#isPriorityEnabled(RulePriority)} instead.
     */
    @Deprecated
    public static boolean isPriorityActive(RulePriority priority) {
        return PriorityFilter.getInstance().isPriorityEnabled(priority);
    }

    /**
     * Get all the priorities that are turned on right now.
     * 
     * @return
     * @deprecated not needed, will be removed. Use {@link PriorityFilter#isPriorityEnabled(RulePriority)}.
     */
    @Deprecated
    public static List<RulePriority> getActivePriorites() {
        List<RulePriority> active = new ArrayList<>();
        for (RulePriority priority : UISettings.currentPriorities(true)) {
            if (PriorityFilter.getInstance().getPriorityFilterList().contains(priority.getPriority())) {
                active.add(priority);
            }
        }
        return active;
    }

    /**
     * @deprecated use {@link PriorityFilter#getInstance()} instead.
     */
    @Deprecated
    public static PriorityFilter getPriorityFilter() {
        return PriorityFilter.getInstance();
    }

    /**
     * @deprecated to be removed without replacement.
     */
    @Deprecated
    public static void setPriorityFilter(PriorityFilter priorityFilter) {
        // does nothing
    }

}
