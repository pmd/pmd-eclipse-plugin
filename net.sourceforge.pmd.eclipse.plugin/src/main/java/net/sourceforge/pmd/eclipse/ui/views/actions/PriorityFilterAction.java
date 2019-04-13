/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ViewerFilter;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.plugin.UISettings;
import net.sourceforge.pmd.eclipse.runtime.cmd.ReviewCodeCmd;
import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptor;
import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptorCache;
import net.sourceforge.pmd.eclipse.ui.views.PriorityFilter;
import net.sourceforge.pmd.eclipse.ui.views.ViolationOutline;
import net.sourceforge.pmd.eclipse.ui.views.ViolationOverview;

/**
 * Filters elements by the Marker priorities
 * 
 * @author SebastianRaffel ( 22.05.2005 )
 * @author bremedios (15.9.2010)
 */
public class PriorityFilterAction extends Action {

    private ViolationOutline outlineView;
    private ViolationOverview overviewView;
    private PriorityFilter priorityFilter;
    private final RulePriority priority;
    private static final Logger LOG = Logger.getLogger(PriorityFilterAction.class);

    private PriorityFilterAction(ViewerFilter[] filters, RulePriority thePriority) {
        priority = thePriority;

        setFilterFrom(filters);
        setupActionLook();
    }

    /**
     * Constructor, used for Violations Outline only
     * 
     * @param prio,
     *            the Priority to filter
     * @param view,
     *            the ViolationOutline
     */
    public PriorityFilterAction(RulePriority prio, ViolationOutline view) {
        this(view.getFilters(), prio);
        outlineView = view;
    }

    /**
     * Constructor, used for Violations Overview only
     * 
     * @param prio,
     *            the Priority to filter
     * @param view,
     *            the violations Overview
     */
    public PriorityFilterAction(RulePriority prio, ViolationOverview view) {
        this(view.getViewer().getFilters(), prio);
        overviewView = view;
    }

    private void setFilterFrom(ViewerFilter[] filters) {

        for (Object filter : filters) {
            if (filter instanceof PriorityFilter) {
                priorityFilter = (PriorityFilter) filter;
            }
        }
    }

    /**
     * Setup the Actions Look by giving the right Image, Text and ToolTip-Text to it, depending on its Priority
     */
    private void setupActionLook() {
        PriorityDescriptor desc = PriorityDescriptorCache.INSTANCE.descriptorFor(priority);
        setImageDescriptor(ImageDescriptor.createFromImage(desc.getImage(16)));
        setText(desc.label);
        String toolTip = String.format(desc.filterText, UISettings.labelFor(priority));
        setToolTipText(toolTip);
    }

    /**
     * @return the Style, in which the Button is displayed
     */
    public int getStyle() {
        return AS_CHECK_BOX;
    }

    /**
     * Executes the Action
     */
    public void run() {
        // we add or remove an Integer with the Priority to a List
        // of Priorities, the Filter does the Rest
        if (isChecked()) {
            priorityFilter.addPriorityToList(priority.getPriority());
        } else {
            priorityFilter.removePriorityFromList(priority.getPriority());
        }

        if (outlineView != null) {
            outlineView.refresh();
        } else if (overviewView != null) {
            overviewView.refresh();
        }

        /* Get all the opened files and tell them to run a "review code" on the file */
        try {
            ReviewCodeCmd.runCodeReviewOnFiles(PMDPlugin.getDefault().getOpenFiles());
        } catch (RuntimeException e) {
            LOG.error(e);
        }
    }

}
