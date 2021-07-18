/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.actions;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.views.ViolationOverview;

/**
 * Allows to Switch between Package and Files or Files only view in the Violation Overview.
 * 
 * @author SebastianRaffel ( 22.05.2005 )
 */
public class PackageSwitchAction extends AbstractPMDAction {

    private ViolationOverview violationView;

    public PackageSwitchAction(ViolationOverview view) {
        violationView = view;
    }

    @Override
    protected String imageId() {
        return PMDUiConstants.ICON_BUTTON_FILES;
    }

    @Override
    protected String tooltipMsgId() {
        return StringKeys.VIEW_TOOLTIP_PACKAGES_FILES;
    }

    /**
     * @return the Style, in which the Button is displayed
     */
    @Override
    public int getStyle() {
        return AS_CHECK_BOX;
    }

    /**
     * Executes the Action.
     */
    @Override
    public void run() {
        // we simply use Functions declared in the Violations Overview
        // the View itself does the Rest, when refreshed
        /*
         * if (isChecked()) { violationView.setPackageFiltered(true); } else { violationView.setPackageFiltered(false);
         * }
         */
        violationView.refresh();
    }

    /**
     * Sets an Action-Button as checked or unchecked.
     * 
     * @param checked,
     *            true, if the Action should be checked, false otherwise
     */
    @Override
    public void setChecked(boolean checked) {
        // we use this Function to change the Images of the Action's Button

        setImageDescriptor(PMDPlugin
                .getImageDescriptor(checked ? PMDUiConstants.ICON_BUTTON_PACKFILES : PMDUiConstants.ICON_BUTTON_FILES));

        super.setChecked(checked);
    }
}
