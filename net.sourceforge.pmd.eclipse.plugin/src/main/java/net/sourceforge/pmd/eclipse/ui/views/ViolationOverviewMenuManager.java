/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.services.IDisposable;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.plugin.UISettings;
import net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord;
import net.sourceforge.pmd.eclipse.ui.model.ProjectRecord;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.views.actions.CalculateStatisticsAction;
import net.sourceforge.pmd.eclipse.ui.views.actions.CollapseAllAction;
import net.sourceforge.pmd.eclipse.ui.views.actions.PriorityFilterAction;
import net.sourceforge.pmd.eclipse.ui.views.actions.ProjectFilterAction;
import net.sourceforge.pmd.eclipse.ui.views.actions.ViolationPresentationTypeAction;
import net.sourceforge.pmd.lang.rule.RulePriority;

/**
 *
 *
 * @author Sven
 *
 */
public class ViolationOverviewMenuManager implements IDisposable {
    private final ViolationOverview overview;
    private PriorityFilterAction[] priorityActions;
    private MenuManager contextMenuManager;

    public ViolationOverviewMenuManager(ViolationOverview overview) {
        this.overview = overview;
    }

    /**
     * Setup the Actions for the ActionBars.
     */
    public void setupActions() {
        RulePriority[] priorities = UISettings.currentPriorities(true);
        priorityActions = new PriorityFilterAction[priorities.length];

        // create the Actions for the PriorityFilter
        for (int i = 0; i < priorities.length; i++) {
            priorityActions[i] = new PriorityFilterAction(priorities[i], overview); // NOPMD by Herlin on 09/10/06 15:02
            priorityActions[i].setChecked(PriorityFilter.getInstance().isPriorityEnabled(priorities[i]));
        }
    }

    /**
     * Creates the ActionBars.
     */
    public void createActionBars(IToolBarManager manager) {
        // Action for calculating the #violations/loc
        final Action calculateStats = new CalculateStatisticsAction(overview);
        manager.add(calculateStats);
        manager.add(new Separator());

        // the PriorityFilter-Actions
        for (PriorityFilterAction priorityAction : priorityActions) {
            manager.add(priorityAction);
        }
        manager.add(new Separator());

        Action collapseAllAction = new CollapseAllAction(overview.getViewer());
        manager.add(collapseAllAction);
    }

    /**
     * Creates the DropDownMenu.
     */
    public void createDropDownMenu(IMenuManager manager) {
        manager.removeAll();

        // both, Context- and DropDownMenu contain the same
        // SubMenu for filtering Projects
        createProjectFilterMenu(manager);
        createShowTypeSubmenu(manager);
    }

    /**
     * Creates the Context Menu.
     */
    public void createContextMenu() {
        if (contextMenuManager == null) {
            contextMenuManager = new MenuManager();
            contextMenuManager.setRemoveAllWhenShown(true);
            contextMenuManager.addMenuListener(new IMenuListener() {
                @Override
                public void menuAboutToShow(IMenuManager manager) {
                    MenuManager submenuManager;

                    // one SubMenu for filtering Projects
                    submenuManager = new MenuManager(getString(StringKeys.VIEW_MENU_RESOURCE_FILTER));
                    createProjectFilterMenu(submenuManager);
                    manager.add(submenuManager);

                    // ... another one for filtering Priorities
                    submenuManager = new MenuManager(getString(StringKeys.VIEW_MENU_PRIORITY_FILTER));
                    for (PriorityFilterAction priorityAction : priorityActions) {
                        submenuManager.add(priorityAction);
                    }
                    manager.add(submenuManager);

                    // ... another one for showing the presentation types
                    submenuManager = new MenuManager(getString(StringKeys.VIEW_MENU_PRESENTATION_TYPE));
                    createShowTypeSubmenu(submenuManager);
                    manager.add(submenuManager);

                    // additions Action: Clear PMD Violations
                    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
                    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end"));

                }
            });
        }

        Tree tree = overview.getViewer().getTree();
        tree.setMenu(contextMenuManager.createContextMenu(tree));

        overview.getSite().registerContextMenu(contextMenuManager, overview.getViewer());
    }

    /**
     * Create the Menu for filtering Projects.
     *
     * @param manager, the MenuManager
     */
    private void createProjectFilterMenu(IMenuManager manager) {
        final List<AbstractPMDRecord> projectFilterList = this.overview.getProjectFilterList();
        final List<ProjectRecord> projectList = new ArrayList<>();

        // We get a List of all Projects
        final AbstractPMDRecord[] projects = this.overview.getAllProjects();
        for (int i = 0; i < projects.length; i++) {
            final ProjectRecord project = (ProjectRecord) projects[i];
            // if the Project contains Errors,
            // we add a FilterAction for it
            if (project.hasMarkers()) {
                final Action projectFilterAction = new ProjectFilterAction(project, this.overview); // NOPMD by Herlin on 09/10/06 15:03

                // if it is not already in the List,
                // we set it as "visible"
                if (!projectFilterList.contains(projects[i])) { // NOPMD by Herlin on 09/10/06 15:04
                    projectFilterAction.setChecked(true);
                }

                manager.add(projectFilterAction);
                projectList.add(project);
            }
        }
        manager.add(new Separator());
    }

    /**
     * Create menu for selecting the show type.
     * @param manager
     */
    private void createShowTypeSubmenu(IMenuManager manager) {
        final Action typeAction1 = new ViolationPresentationTypeAction(this.overview, ViolationOverview.SHOW_MARKERS_FILES);
        final Action typeAction2 = new ViolationPresentationTypeAction(this.overview, ViolationOverview.SHOW_FILES_MARKERS);
        final Action typeAction3 = new ViolationPresentationTypeAction(this.overview, ViolationOverview.SHOW_PACKAGES_FILES_MARKERS);
        manager.add(typeAction1);
        manager.add(typeAction2);
        manager.add(typeAction3);
    }

    /**
     * Helper method to return an NLS string from its key.
     */
    private String getString(String key) {
        return PMDPlugin.getDefault().getStringTable().getString(key);
    }

    @Override
    public void dispose() {
        if (contextMenuManager != null) {
            contextMenuManager.dispose();
            contextMenuManager = null;
        }
    }
}
