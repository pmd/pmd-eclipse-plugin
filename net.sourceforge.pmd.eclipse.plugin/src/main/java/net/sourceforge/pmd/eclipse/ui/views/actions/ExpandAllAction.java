/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.actions;

import org.eclipse.jface.viewers.TreeViewer;

import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 * Collapses the Violation Overview Tree.
 * 
 * @author SebastianRaffel ( 22.05.2005 ), Philippe Herlin, Sven Jacob
 *
 */
public class ExpandAllAction extends AbstractPMDAction {

    private final TreeViewer treeViewer;

    public ExpandAllAction(TreeViewer theViewer) {
        super();
        treeViewer = theViewer;
    }

    @Override
    protected String imageId() {
        return PMDUiConstants.ICON_BUTTON_EXPAND;
    }

    @Override
    protected String tooltipMsgId() {
        return StringKeys.VIEW_TOOLTIP_EXPAND_ALL;
    }

    @Override
    public void run() {
        treeViewer.expandAll();
    }
}
