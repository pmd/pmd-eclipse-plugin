/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.cpd2;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.lang.document.FileLocation;

/**
 * 
 *
 */
public class CPDViewTooltipListener2 implements Listener {

    private final CPDView2 view;
    private Cursor normalCursor;
    private Cursor handCursor;

    public CPDViewTooltipListener2(CPDView2 view) {
        this.view = view;
        initialize();
    }

    private void initialize() {
        Display disp = Display.getDefault();
        normalCursor = disp.getSystemCursor(SWT.CURSOR_ARROW);
        handCursor = disp.getSystemCursor(SWT.CURSOR_HAND);
    }

    // open file and jump to the startline
    private void highlight(Match match, Mark entry) {
        FileLocation location = entry.getLocation();

        IPath path = Path.fromOSString(location.getFileId().getOriginalPath());
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
        if (file == null) {
            return;
        }

        try {
            // open editor
            IWorkbenchPage page = view.getSite().getPage();
            IEditorPart part = IDE.openEditor(page, file);
            if (part instanceof ITextEditor) {
                // select text
                ITextEditor textEditor = (ITextEditor) part;
                IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
                int offset = document.getLineOffset(location.getStartLine() - 1);
                int length = document.getLineOffset(location.getStartLine() - 1 + match.getLineCount()) - offset - 1;
                textEditor.selectAndReveal(offset, length);
            }
        } catch (PartInitException | BadLocationException pie) {
            PMDPlugin.getDefault().logError(getString(StringKeys.ERROR_VIEW_EXCEPTION), pie);
        }
    }

    private static Match matchAt(TreeItem treeItem) {
        Object item = ((TreeNode) treeItem.getData()).getValue();
        return item instanceof Match ? (Match) item : null;
    }

    private Mark itemAt(TreeItem treeItem, Point location) {
        if (treeItem == null) {
            return null;
        }

        Object item = ((TreeNode) treeItem.getData()).getValue();

        String[] names;
        if (item instanceof Match) {
            names = CPDViewLabelProvider2.sourcesFor((Match) item);
        } else {
            return null;
        }

        location.x -= view.widthOf(0); // subtract width of preceeding columns

        int colWidth = view.widthOf(CPDView2.SOURCE_COLUMN_IDX);
        int cellWidth = colWidth / names.length;

        for (int i = 0; i < names.length; i++) {
            int rightEdge = colWidth - (cellWidth * i);
            int[] widths = view.widthsFor(names[i]);
            if (widths == null) {
                continue;
            }
            int classWidth = widths[1];
            if (location.x > rightEdge - classWidth // right of the start?
                    && location.x < rightEdge) { // left of the end?
                return CPDViewLabelProvider2.entriesFor((Match) item)[i];
            }
        }

        return null;
    }

    @Override
    public void handleEvent(Event event) {
        Tree tree = view.getTreeViewer().getTree();
        Point location = new Point(event.x, event.y);
        Shell shell = tree.getShell();

        if (view.inColumn(location) != CPDView2.SOURCE_COLUMN_IDX) {
            shell.setCursor(normalCursor);
            return;
        }

        TreeItem item = tree.getItem(location);
        Mark entry = itemAt(item, location);
        if (entry == null) {
            shell.setCursor(normalCursor);
            return;
        }

        switch (event.type) {
        case SWT.MouseDown:
            highlight(matchAt(item), entry);
            break;
        case SWT.MouseMove:
        case SWT.MouseHover:
            shell.setCursor(handCursor);
            break;
        default:
            break;
        }

    }

    /**
     * Helper method to return an NLS string from its key.
     */
    private String getString(String key) {
        return PMDPlugin.getDefault().getStringTable().getString(key);
    }
}
