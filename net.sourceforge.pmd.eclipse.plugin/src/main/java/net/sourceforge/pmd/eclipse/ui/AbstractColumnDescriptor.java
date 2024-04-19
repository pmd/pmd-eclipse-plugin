/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui;

import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import net.sourceforge.pmd.eclipse.util.ResourceManager;
import net.sourceforge.pmd.eclipse.util.internal.SWTUtil;

/**
 * 
 * @author Brian Remedios
 */
public abstract class AbstractColumnDescriptor implements ColumnDescriptor {

    private final String id;
    private final String label;
    private final String tooltip;
    private final int alignment;
    private final int width;
    private final boolean isResizable;
    private final String imagePath;

    public static final String DESCRIPTOR_KEY = "descriptor";

    public AbstractColumnDescriptor(String theId, String labelKey, int theAlignment, int theWidth,
            boolean resizableFlag, String theImagePath) {
        super();

        id = theId;
        label = SWTUtil.stringFor(labelKey);
        tooltip = SWTUtil.tooltipFor(labelKey);
        alignment = theAlignment;
        width = theWidth;
        isResizable = resizableFlag;
        imagePath = theImagePath;
    }

    protected void setLabelIfImageMissing(TreeColumn column) {
        if (imagePath == null) {
            column.setText(label);
        }
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String tooltip() {
        return tooltip;
    }

    @Override
    public int defaultWidth() {
        return width;
    }

    protected TreeColumn buildTreeColumn(Tree parent) {

        TreeColumn tc = new TreeColumn(parent, alignment);
        loadCommon(tc);
        tc.setWidth(width);
        tc.setResizable(isResizable);
        tc.setToolTipText(tooltip);

        return tc;
    }

    public TableColumn buildTableColumn(Table parent) {

        TableColumn tc = new TableColumn(parent, alignment);
        loadCommon(tc);
        tc.setText(label);
        tc.setWidth(width);
        tc.setResizable(isResizable);
        tc.setToolTipText(tooltip);

        return tc;
    }

    private void loadCommon(Item column) {
        column.setData(DESCRIPTOR_KEY, this);
        if (imagePath != null) {
            column.setImage(ResourceManager.imageFor(imagePath));
        }
    }
}
