/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.eclipse.ui.ColumnDescriptor;

/**
 *
 * @author Brian Remedios
 */
public interface RuleColumnDescriptor extends ColumnDescriptor {

    RuleFieldAccessor accessor();

    Image imageFor(Rule rule);

    Image imageFor(RuleCollection collection);

    String stringValueFor(Rule rule);

    String stringValueFor(RuleCollection collection);

    String detailStringFor(Rule rule);

    String detailStringFor(RuleGroup group);

    TableColumn newTableColumnFor(Table parent, int columnIndex, SortListener sortListener,
            Map<Integer, List<Listener>> paintListeners);

    TreeColumn newTreeColumnFor(Tree parent, int columnIndex, SortListener sortListener,
            Map<Integer, List<Listener>> paintListeners);
}
