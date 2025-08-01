/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.ast;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;

import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.model.FileRecord;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.views.AbstractResourceView;
import net.sourceforge.pmd.eclipse.ui.views.AbstractStructureInspectorPage;
import net.sourceforge.pmd.eclipse.ui.views.actions.CollapseAllAction;
import net.sourceforge.pmd.eclipse.ui.views.actions.ExpandAllAction;

/**
 * 
 * @author Brian Remedios
 */
public class ASTView extends AbstractResourceView {

    private ASTViewPage page;
    private Action toggleShowImportsAction;
    private Action toggleShowCommentsAction;

    private static final String SHOW_IMPORTS = "ASTView.showImports";
    private static final String SHOW_COMMENTS = "ASTView.showComments";

    static boolean showImports() {
        return getBoolUIPref(SHOW_IMPORTS);
    }

    static boolean showComments() {
        return getBoolUIPref(SHOW_COMMENTS);
    }

    @Override
    protected String pageMessageId() {
        return StringKeys.VIEW_AST_DEFAULT_TEXT;
    }

    @Override
    protected String mementoFileId() {
        return PMDUiConstants.MEMENTO_AST_FILE;
    }

    @Override
    protected PageRec doCreatePage(IWorkbenchPart part) {

        FileRecord resourceRecord = getFileRecordFromWorkbenchPart(part);
        if (resourceRecord != null) {
            setupListener(resourceRecord);

            // creates a new ASTViewPage, when a Resource exists
            page = new ASTViewPage(part, resourceRecord);
            initPage(page);
            page.createControl(getPageBook());

            makeActions();
            addToolbarControls();

            return new PageRec(part, page);
        }
        return null;
    }

    @Override
    protected AbstractStructureInspectorPage getCurrentViewPage() {
        return getCurrentASTViewPage();
    }

    public void showImports(boolean flag) {
        setUIPref(SHOW_IMPORTS, flag);
        page.showImports(flag);
    }

    public void showComments(boolean flag) {
        setUIPref(SHOW_COMMENTS, flag);
        page.showComments(flag);
    }

    /**
     * @return the currently displayed Page
     */
    private ASTViewPage getCurrentASTViewPage() {
        IPage page = super.getCurrentPage();
        if (!(page instanceof ASTViewPage)) {
            return null;
        }

        return (ASTViewPage) page;
    }

    private void makeActions() {
        toggleShowImportsAction = new Action() {
            @Override
            public void run() {
                boolean show = !showImports();
                setChecked(show);
                showImports(show);
            }
        };
        toggleShowImportsAction.setText("Show imports");
        toggleShowImportsAction.setChecked(showImports());

        toggleShowCommentsAction = new Action() {
            @Override
            public void run() {
                boolean show = !showComments();
                setChecked(show);
                showComments(show);
            }
        };
        toggleShowCommentsAction.setText("Show comments");
        toggleShowCommentsAction.setChecked(showComments());
    }

    private void addToolbarControls() {
        IActionBars aBars = getViewSite().getActionBars();

        IToolBarManager manager = aBars.getToolBarManager();
        manager.add(new ExpandAllAction(page.astViewer()));
        manager.add(new CollapseAllAction(page.astViewer()));

        addViewFilterOptions(aBars);
    }

    private void addViewFilterOptions(IActionBars aBars) {
        IMenuManager mmgr = aBars.getMenuManager();
        mmgr.add(toggleShowImportsAction);
        mmgr.add(toggleShowCommentsAction);
    }
}
