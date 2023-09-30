/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.cpd;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.cmd.internal.CpdMarkWithSourceCode;
import net.sourceforge.pmd.eclipse.runtime.cmd.internal.CpdMatchWithSourceCode;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.lang.document.FileLocation;

/**
 * 
 * @author Sven
 *
 */

public class CPDViewDoubleClickEventListener implements IDoubleClickListener {

    private final CPDView view;

    public CPDViewDoubleClickEventListener(CPDView view) {
        this.view = view;
    }

    @Override
    public void doubleClick(DoubleClickEvent event) {
        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        final Object object = selection.getFirstElement();

        final TreeNode node = (TreeNode) object;
        final Object value = node.getValue();
        final TreeViewer treeViewer = view.getTreeViewer();

        if (value instanceof CpdMatchWithSourceCode) {
            if (treeViewer.getExpandedState(node)) {
                // the node is expanded, so collapse
                treeViewer.collapseToLevel(node, TreeViewer.ALL_LEVELS);
            } else {
                // the node is collapsed, so expand
                treeViewer.expandToLevel(node, 1);
            }
        } else if (value instanceof CpdMarkWithSourceCode) {
            final CpdMarkWithSourceCode entry = (CpdMarkWithSourceCode) value;
            final CpdMatchWithSourceCode match = (CpdMatchWithSourceCode) node.getParent().getValue();
            highlightText(match, entry);
        }
    }

    private void highlightText(CpdMatchWithSourceCode matchWithSource, CpdMarkWithSourceCode markWithSource) {
        // open file and jump to the startline
        Match match = matchWithSource.getMatch();
        Mark entry = markWithSource.getMark();
        FileLocation location = entry.getLocation();

        final IPath path = Path.fromOSString(location.getFileId().getOriginalPath());
        final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
        if (file == null) {
            return;
        }

        try {
            // open editor
            final IWorkbenchPage page = this.view.getSite().getPage();
            final IEditorPart part = IDE.openEditor(page, file);
            if (part instanceof ITextEditor) {
                // select text
                final ITextEditor textEditor = (ITextEditor) part;
                final IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
                final int offset = document.getLineOffset(location.getStartLine() - 1);
                final int length = document.getLineOffset(location.getStartLine() - 1 + match.getLineCount()) - offset - 1;
                textEditor.selectAndReveal(offset, length);
            }
        } catch (PartInitException | BadLocationException pie) {
            PMDPlugin.getDefault().logError(getString(StringKeys.ERROR_VIEW_EXCEPTION), pie);
        }
    }

    /**
     * Helper method to return an NLS string from its key.
     */
    private String getString(String key) {
        return PMDPlugin.getDefault().getStringTable().getString(key);
    }

}
