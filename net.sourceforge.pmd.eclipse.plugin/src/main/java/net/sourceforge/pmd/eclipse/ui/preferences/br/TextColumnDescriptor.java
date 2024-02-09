/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.util.AbstractCellPainterBuilder;
import net.sourceforge.pmd.eclipse.util.ResourceManager;
import net.sourceforge.pmd.eclipse.util.Util;
import net.sourceforge.pmd.lang.rule.Rule;

/**
 *
 * @author Brian Remedios
 */
public class TextColumnDescriptor extends SimpleColumnDescriptor {

    public static final RuleFieldAccessor RULE_SET_NAME_ACCESSOR = new BasicRuleFieldAccessor() {
        @Override
        public Comparable<?> valueFor(Rule rule) {
            return RuleUIUtil.ruleSetNameFrom(rule);
        }
    };

    public static final RuleFieldAccessor PROPERTIES_ACCESSOR = new BasicRuleFieldAccessor() {
        @Override
        public Comparable<?> valueFor(Rule rule) {
            return RuleUIUtil.propertyStringFrom(rule, "*");
        }
    };

    private static final int IMG_OFFSET = 14;

    public TextColumnDescriptor(String theId, String theLabel, int theAlignment, int theWidth,
            RuleFieldAccessor theAccessor, boolean resizableFlag, String theImagePath) {
        super(theId, theLabel, theAlignment, theWidth, theAccessor, resizableFlag, theImagePath);
    }

    private static boolean isCheckboxTree(Tree tree) {
        return (tree.getStyle() | SWT.CHECK) > 0;
    }

    @Override
    public TreeColumn newTreeColumnFor(Tree parent, int columnIndex, SortListener sortListener,
            Map<Integer, List<Listener>> paintListeners) {

        TreeColumn tc = super.newTreeColumnFor(parent, columnIndex, sortListener, paintListeners);

        if (isCheckboxTree(parent) && columnIndex != 0) {
            // can't owner-draw the check or expansion toggles
            addPainterFor(tc.getParent(), columnIndex, accessor(), paintListeners);
        }

        return tc;
    }

    @Override
    public String stringValueFor(Rule rule) {
        return ""; // we draw it ourselves
    }

    @Override
    public String stringValueFor(RuleCollection collection) {
        return ""; // we draw it ourselves
    }

    @Override
    public Image imageFor(Rule rule) {
        boolean hasIssues = rule.dysfunctionReason() != null;

        return hasIssues ? ResourceManager.imageFor(PMDUiConstants.ICON_WARN) : null;
    }

    public void addPainterFor(final Tree tree, final int columnIndex, final RuleFieldAccessor getter,
            Map<Integer, List<Listener>> thePaintListeners) {

        CellPainterBuilder cpl = new AbstractCellPainterBuilder() {
            @Override
            public void addPainterFor(final Tree tree, final int columnIndex, final RuleFieldAccessor getter,
                    Map<Integer, List<Listener>> paintListeners) {

                Listener paintListener = new Listener() {
                    @Override
                    public void handleEvent(Event event) {

                        if (event.index != columnIndex) {
                            return;
                        }

                        Object value = ((TreeItem) event.item).getData();
                        if (value == null || value instanceof RuleCollection) {
                            return;
                        }

                        GC gc = event.gc;

                        int imgOffset;

                        Rule rule = (Rule) value;
                        gc.setFont(fontFor(tree, rule));
                        imgOffset = rule.dysfunctionReason() != null ? IMG_OFFSET : 0;

                        String text = textFor((TreeItem) event.item, getter);
                        int descent = gc.getFontMetrics().getDescent();

                        gc.drawString(text, event.x + imgOffset, event.y + descent, true);
                    }
                };

                Listener measureListener = new Listener() {
                    @Override
                    public void handleEvent(Event event) {
                        if (event.index != columnIndex) {
                            return;
                        }

                        String text = textFor((TreeItem) event.item, getter);
                        if (text == null) {
                            text = "";
                        }

                        Point size = event.gc.textExtent(text);
                        event.width = size.x + 2 * 3;
                        // event.height = Math.max(event.height, size.y + (3));
                    }
                };

                Util.addListener(tree, SWT.PaintItem, paintListener, paintListeners);
                Util.addListener(tree, SWT.MeasureItem, measureListener, paintListeners);
            }
        };

        cpl.addPainterFor(tree, columnIndex, getter, thePaintListeners);
    }
}
