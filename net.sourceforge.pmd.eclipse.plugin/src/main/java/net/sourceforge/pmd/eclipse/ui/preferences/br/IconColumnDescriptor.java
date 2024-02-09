/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.util.ResourceManager;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.lang.rule.RulePriority;

/**
 *
 * @author Brian Remedios
 */
public class IconColumnDescriptor extends AbstractRuleColumnDescriptor {

    private Map<Object, Image> iconsByValue;

    private static final Map<Object, String> ICON_NAMES_BY_PRIORITY = new HashMap<>();

    static {
        ICON_NAMES_BY_PRIORITY.put(RulePriority.LOW, PMDUiConstants.ICON_BUTTON_PRIO5);
        ICON_NAMES_BY_PRIORITY.put(RulePriority.MEDIUM_LOW, PMDUiConstants.ICON_BUTTON_PRIO4);
        ICON_NAMES_BY_PRIORITY.put(RulePriority.MEDIUM, PMDUiConstants.ICON_BUTTON_PRIO3);
        ICON_NAMES_BY_PRIORITY.put(RulePriority.MEDIUM_HIGH, PMDUiConstants.ICON_BUTTON_PRIO2);
        ICON_NAMES_BY_PRIORITY.put(RulePriority.HIGH, PMDUiConstants.ICON_BUTTON_PRIO1);
    }

    private static final Map<Object, String> ICON_NAMES_BY_BOOLEAN = new HashMap<>();

    static {
        ICON_NAMES_BY_BOOLEAN.put(Boolean.TRUE, PMDUiConstants.ICON_GREEN_CHECK);
        ICON_NAMES_BY_BOOLEAN.put(Boolean.FALSE, PMDUiConstants.ICON_EMPTY);
    }

    public static final RuleColumnDescriptor PRIORITY = new IconColumnDescriptor("iPriority",
            StringKeys.PREF_RULESET_COLUMN_PRIORITY, SWT.RIGHT, 53, RuleFieldAccessor.PRIORITY, true,
            PMDUiConstants.ICON_BUTTON_PRIO0, ICON_NAMES_BY_PRIORITY);

    public IconColumnDescriptor(String theId, String labelKey, int theAlignment, int theWidth,
            RuleFieldAccessor theAccessor, boolean resizableFlag, String theImagePath,
            Map<Object, String> imageNamesByValue) {
        super(theId, labelKey, theAlignment, theWidth, theAccessor, resizableFlag, theImagePath);

        iconsByValue = iconsFor(imageNamesByValue);
    }

    private static Map<Object, Image> iconsFor(Map<Object, String> imageNamesByValue) {

        Map<Object, Image> imagesByValue = new HashMap<>(imageNamesByValue.size());
        for (Map.Entry<Object, String> entry : imageNamesByValue.entrySet()) {
            imagesByValue.put(entry.getKey(), ResourceManager.imageFor(entry.getValue()));
        }
        return imagesByValue;
    }

    @Override
    public Image imageFor(Rule rule) {
        Object value = valueFor(rule);
        return iconsByValue.get(value);
    }

    @Override
    public Image imageFor(RuleCollection collection) {
        Object value = valueFor(collection);
        return iconsByValue.get(value);
    }

    @Override
    public TreeColumn newTreeColumnFor(Tree parent, int columnIndex, SortListener sortListener,
            Map<Integer, List<Listener>> paintListeners) {
        return buildTreeColumn(parent, sortListener);
    }

    @Override
    public String stringValueFor(Rule rule) {
        return null;
    }

}
