/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.actions;

import org.eclipse.jface.viewers.TreeViewer;

import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 * Collapses the Violation Overview Tree
 * 
 * @author SebastianRaffel ( 22.05.2005 ), Philippe Herlin, Sven Jacob
 *
 */
public class CollapseAllAction extends AbstractPMDAction {

    private final TreeViewer treeViewer;

    public CollapseAllAction(TreeViewer theViewer) {
        super();
        treeViewer = theViewer;
    }

    protected String imageId() {
        return PMDUiConstants.ICON_BUTTON_COLLAPSE;
    }

    protected String tooltipMsgId() {
        return StringKeys.VIEW_TOOLTIP_COLLAPSE_ALL;
    }

    public void run() {
        treeViewer.collapseAll();
    }
}
