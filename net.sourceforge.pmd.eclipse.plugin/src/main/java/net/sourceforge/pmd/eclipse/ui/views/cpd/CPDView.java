/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.cpd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.PMDRuntimeConstants;
import net.sourceforge.pmd.eclipse.runtime.cmd.internal.CpdMarkWithSourceCode;
import net.sourceforge.pmd.eclipse.runtime.cmd.internal.CpdMatchWithSourceCode;
import net.sourceforge.pmd.eclipse.runtime.cmd.internal.CpdResult;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 * A class for showing the Copy / Paste Detection View.
 *
 * @author Sven
 *
 */

public class CPDView extends ViewPart implements IPropertyListener {
    private TreeViewer treeViewer;
    private TreeNodeContentProvider contentProvider;
    private CPDViewLabelProvider labelProvider;
    private CPDViewDoubleClickEventListener doubleClickListener;
    private CPDViewTooltipListener tooltipListener;
    private static final int MAX_MATCHES = 100;

    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        contentProvider = new TreeNodeContentProvider();
        labelProvider = new CPDViewLabelProvider();
        doubleClickListener = new CPDViewDoubleClickEventListener(this);
        tooltipListener = new CPDViewTooltipListener(this);
    }

    @Override
    public void createPartControl(Composite parent) {
        int treeStyle = SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION;
        treeViewer = new TreeViewer(parent, treeStyle);
        treeViewer.setUseHashlookup(true);
        Tree tree = treeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        treeViewer.setContentProvider(contentProvider);
        treeViewer.setLabelProvider(labelProvider);
        treeViewer.addDoubleClickListener(doubleClickListener);

        tooltipListener.initialize();
        tree.addListener(SWT.Dispose, tooltipListener);
        tree.addListener(SWT.KeyDown, tooltipListener);
        tree.addListener(SWT.MouseMove, tooltipListener);
        tree.addListener(SWT.MouseHover, tooltipListener);
        createColumns(tree);
    }

    /**
     * Creates the columns of the tree.
     * @param tree Tree from the treeViewer
     */
    private void createColumns(Tree tree) {
        // the "+"-sign for expanding packages
        final TreeColumn plusColumn = new TreeColumn(tree, SWT.RIGHT);
        plusColumn.setWidth(20);
        //      plusColumn.setResizable(false);

        // shows the image
        TreeColumn imageColumn = new TreeColumn(tree, SWT.CENTER);
        imageColumn.setWidth(20);
        //      imageColumn.setResizable(false);

        // shows the message
        TreeColumn messageColumn = new TreeColumn(tree, SWT.LEFT);
        messageColumn.setText(getString(StringKeys.VIEW_COLUMN_MESSAGE));
        messageColumn.setWidth(300);

        // shows the class
        TreeColumn classColumn = new TreeColumn(tree, SWT.LEFT);
        classColumn.setText(getString(StringKeys.VIEW_COLUMN_CLASS));
        classColumn.setWidth(300);

    }

    /**
     * @return the tree viewer.
     */
    public TreeViewer getTreeViewer() {
        return treeViewer;
    }

    /**
     * Helper method to return an NLS string from its key.
     */
    private String getString(String key) {
        return PMDPlugin.getDefault().getStringTable().getString(key);
    }

    @Override
    public void setFocus() {
        treeViewer.getTree().setFocus();
    }

    /**
     * Sets input for the table.
     * @param result CPD results with matches and source code slices from the CPD
     */
    public void setData(CpdResult result) {
        List<TreeNode> elements = new ArrayList<>();
        // iterate the matches
        for (int count = 0; count < result.getMatches().size() && count < MAX_MATCHES; count++) {
            Match match = result.getMatches().get(count);
            CpdMatchWithSourceCode data = new CpdMatchWithSourceCode(result, match);

            // create a treenode for the match and add to the list
            TreeNode matchNode = new TreeNode(data);
            elements.add(matchNode);

            // create the children of the match
            TreeNode[] children = new TreeNode[match.getMarkCount()];
            Iterator<Mark> entryIterator = match.getMarkSet().iterator();
            for (int j = 0; entryIterator.hasNext(); j++) {
                final CpdMarkWithSourceCode entry = new CpdMarkWithSourceCode(result, entryIterator.next());
                children[j] = new TreeNode(entry);
                children[j].setParent(matchNode);
            }
            matchNode.setChildren(children);
        }

        // set the children of the rootnode: the matches
        treeViewer.setInput(elements.toArray(new TreeNode[0]));
    }

    /**
     * After the CPD command is executed, it will trigger an propertyChanged event.
     */
    @Override
    public void propertyChanged(Object source, int propId) {
        if (propId == PMDRuntimeConstants.PROPERTY_CPD && source instanceof CpdResult) {
            CpdResult result = (CpdResult) source;
            // after setdata(iter) iter.hasNext will always return false
            boolean hasResults = !result.getMatches().isEmpty();
            setData(result);
            if (!hasResults) {
                // no entries
                MessageBox box = new MessageBox(this.treeViewer.getControl().getShell());
                box.setText(getString(StringKeys.DIALOG_CPD_NORESULTS_HEADER));
                box.setMessage(getString(StringKeys.DIALOG_CPD_NORESULTS_BODY));
                box.open();
            }
        }
    }
}
