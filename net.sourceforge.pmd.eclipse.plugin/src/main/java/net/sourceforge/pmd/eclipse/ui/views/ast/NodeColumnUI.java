
package net.sourceforge.pmd.eclipse.ui.views.ast;

import org.eclipse.swt.SWT;

import net.sourceforge.pmd.eclipse.ui.ItemColumnDescriptor;
import net.sourceforge.pmd.eclipse.ui.ItemFieldAccessor;
import net.sourceforge.pmd.eclipse.ui.ItemFieldAccessorAdapter;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.util.Util;
import net.sourceforge.pmd.lang.ast.Node;

/**
 * 
 * @author Brian Remedios
 */
public interface NodeColumnUI {

    ItemFieldAccessor<String, Node> TYPE_NAME_ACC = new ItemFieldAccessorAdapter<String, Node>(Util.COMP_STR) {
        public String valueFor(Node node) {
            return node.toString();
        }
    };

    ItemFieldAccessor<String, Node> IMAGE_ACC = new ItemFieldAccessorAdapter<String, Node>(Util.COMP_STR) {
        public String valueFor(Node node) {
            return node.getImage();
        }
    };

    ItemFieldAccessor<Integer, Node> BEGIN_LINE_NUM_ACC = new ItemFieldAccessorAdapter<Integer, Node>(Util.COMP_INT) {
        public Integer valueFor(Node node) {
            return node.getBeginLine();
        }
    };

    ItemFieldAccessor<Integer, Node> END_LINE_NUM_ACC = new ItemFieldAccessorAdapter<Integer, Node>(Util.COMP_INT) {
        public Integer valueFor(Node node) {
            return node.getEndLine();
        }
    };

    ItemFieldAccessor<Integer, Node> BEGIN_COLUMN_ACC = new ItemFieldAccessorAdapter<Integer, Node>(Util.COMP_INT) {
        public Integer valueFor(Node node) {
            return node.getBeginColumn();
        }
    };

    ItemFieldAccessor<Integer, Node> END_COLUMN_ACC = new ItemFieldAccessorAdapter<Integer, Node>(Util.COMP_INT) {
        public Integer valueFor(Node node) {
            return node.getEndColumn();
        }
    };

    ItemFieldAccessor<String, Node> DERIVED_ACC = new ItemFieldAccessorAdapter<String, Node>(Util.COMP_STR) {
        public String valueFor(Node node) {
            return NodeImageDeriver.derivedTextFor(node);
        }
    };

    ItemFieldAccessor<String, Node> IMAGE_OR_DERIVED_ACC = new ItemFieldAccessorAdapter<String, Node>(Util.COMP_STR) {
        public String valueFor(Node node) {
            return node.getImage() == null ? NodeImageDeriver.derivedTextFor(node) : node.getImage();
        }
    };

    ItemColumnDescriptor TYPE_NAME = new ItemColumnDescriptor("", StringKeys.NODE_COLUMN_NAME, SWT.LEFT, 85, true,
            TYPE_NAME_ACC);
    ItemColumnDescriptor IMAGE_DATA = new ItemColumnDescriptor("", StringKeys.NODE_IMAGE_DATA, SWT.LEFT, 25, true,
            IMAGE_ACC);
    ItemColumnDescriptor LINE_NUM = new ItemColumnDescriptor("", StringKeys.NODE_LINE_NUM, SWT.RIGHT, 35, true,
            BEGIN_LINE_NUM_ACC);
    ItemColumnDescriptor DERIVED = new ItemColumnDescriptor("", StringKeys.NODE_DERIVED, SWT.LEFT, 25, true,
            DERIVED_ACC);
    ItemColumnDescriptor IMAGE_OR_DERIVED = new ItemColumnDescriptor("", StringKeys.NODE_IMG_OR_DERIVED, SWT.LEFT, 25,
            true, IMAGE_OR_DERIVED_ACC);

    ItemColumnDescriptor[] VISIBLE_COLUMNS = new ItemColumnDescriptor[] { LINE_NUM, TYPE_NAME, IMAGE_OR_DERIVED };

}
