/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.actions;

import org.eclipse.jface.action.Action;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.views.ViolationOverview;

/**
 * 
 * @author Sven
 *
 */
public class ViolationPresentationTypeAction extends Action {
    private final ViolationOverview overview;
    private final int type;

    public ViolationPresentationTypeAction(ViolationOverview overview, int type) {
        super();
        this.overview = overview;
        this.type = type;

        setChecked(overview.getShowType() == type);
        switch (type) {
        case ViolationOverview.SHOW_FILES_MARKERS: // we set Image and Text for the Action
            setImageDescriptor(PMDPlugin.getImageDescriptor(PMDUiConstants.ICON_BUTTON_FILEMARKERS));
            setText(AbstractPMDAction.getString(StringKeys.VIEW_MENU_FILEMARKERS));
            break;
        case ViolationOverview.SHOW_MARKERS_FILES:
            setImageDescriptor(PMDPlugin.getImageDescriptor(PMDUiConstants.ICON_BUTTON_MARKERFILES));
            setText(AbstractPMDAction.getString(StringKeys.VIEW_MENU_MARKERFILES));
            break;
        case ViolationOverview.SHOW_PACKAGES_FILES_MARKERS:
            setImageDescriptor(PMDPlugin.getImageDescriptor(PMDUiConstants.ICON_BUTTON_PACKFILES));
            setText(AbstractPMDAction.getString(StringKeys.VIEW_MENU_PACKFILES));
            break;
        default: // do nothing
        }
    }

    @Override
    public int getStyle() {
        return AS_RADIO_BUTTON;
    }

    /**
     * Executes the Action.
     */
    @Override
    public void run() {
        this.overview.setShowType(this.type);
        this.overview.refresh();
    }
}
