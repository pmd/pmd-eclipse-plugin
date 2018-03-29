package net.sourceforge.pmd.eclipse.util;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.eclipse.plugin.UISettings;
import net.sourceforge.pmd.eclipse.ui.views.PriorityFilter;

/**
 * Priority Util is a manager of the available priorities in the app. This Util
 * will update if the Violations Overview Priority Filter updates which will
 * cause the File markers, Tree Markers, and Violation Outline markers to
 * update.
 * 
 * @author Phillip Krall
 *
 */
public class PriorityUtil {

	private static PriorityFilter priorityFilter = new PriorityFilter();
	
	private PriorityUtil() {
	}

	/**
	 * Check if a priority is turned on or not
	 * 
	 * @param priority
	 * @return
	 */
	public static boolean isPriorityActive(RulePriority priority) {
		return getActivePriorites().contains(priority);
	}

	/**
	 * Get all the priorities that are turned on right now.
	 * 
	 * @return
	 */
	public static List<RulePriority> getActivePriorites() {
		List<RulePriority> active = new ArrayList<RulePriority>();
		for (RulePriority priority : UISettings.currentPriorities(true)) {
			if (priorityFilter.getPriorityFilterList().contains(priority.getPriority())) {
				active.add(priority);
			}
		}
		return active;
	}

	public static PriorityFilter getPriorityFilter() {
		return priorityFilter;
	}

	public static void setPriorityFilter(PriorityFilter priorityFilter) {
		PriorityUtil.priorityFilter = priorityFilter;
	}

}
