/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;

/**
 * Creates the PMD Perspective.
 * 
 * @author SebastianRaffel ( 08.05.2005 )
 */
public class PMDPerspective implements IPerspectiveFactory {

    @Override
    public void createInitialLayout(IPageLayout layout) {
        String editorAreaId = layout.getEditorArea();
        String explorerAreaId = "org.eclipse.jdt.ui.PackageExplorer";
        String outlineAreaId = PMDUiConstants.ID_OUTLINE;
        String overviewAreaId = PMDUiConstants.ID_OVERVIEW;

        layout.addView(explorerAreaId, IPageLayout.LEFT, 0.25f, editorAreaId);
        layout.addView(outlineAreaId, IPageLayout.BOTTOM, 0.6f, explorerAreaId);
        layout.addView(overviewAreaId, IPageLayout.BOTTOM, 0.65f, editorAreaId);
    }
}
