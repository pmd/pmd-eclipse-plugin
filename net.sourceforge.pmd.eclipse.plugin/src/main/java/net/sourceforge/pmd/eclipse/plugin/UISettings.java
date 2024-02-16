/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferencesManager;
import net.sourceforge.pmd.eclipse.ui.Shape;
import net.sourceforge.pmd.eclipse.ui.ShapeDescriptor;
import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptorCache;
import net.sourceforge.pmd.eclipse.util.FontBuilder;
import net.sourceforge.pmd.lang.rule.RulePriority;

/**
 * 
 * @author Brian Remedios
 */
public final class UISettings {

    public static final FontBuilder CODE_FONT_BUILDER = new FontBuilder("Courier", 11, SWT.NORMAL);

    private static Map<RulePriority, String> labelsByPriority = new HashMap<>();

    private static IPreferencesManager preferencesManager = PMDPlugin.getDefault().getPreferencesManager();

    private UISettings() {
        // utility class
    }


    public static Shape[] allShapes() {
        return new Shape[] { Shape.circle, Shape.star, Shape.domeLeft, Shape.domeRight, Shape.diamond, Shape.square,
            Shape.roundedRect, Shape.minus, Shape.pipe, Shape.plus, Shape.triangleUp, Shape.triangleDown,
            Shape.triangleRight, Shape.triangleLeft, Shape.triangleNorthEast, Shape.triangleSouthEast,
            Shape.triangleSouthWest, Shape.triangleNorthWest, };
    }

    public static RulePriority[] currentPriorities(boolean sortAscending) {

        RulePriority[] priorities = RulePriority.values();

        Arrays.sort(priorities, new Comparator<RulePriority>() {
            @Override
            public int compare(RulePriority rpA, RulePriority rbB) {
                return rpA.getPriority() - rbB.getPriority();
            }
        });
        return priorities;
    }

    public static Map<Shape, ShapeDescriptor> shapeSet(RGB color, int size) {

        Map<Shape, ShapeDescriptor> shapes = new HashMap<>();

        for (Shape shape : EnumSet.allOf(Shape.class)) {
            shapes.put(shape, new ShapeDescriptor(shape, color, size));
        }

        return shapes;
    }

    private static String pLabelFor(RulePriority priority, boolean useCustom) {

        if (!useCustom) {
            return priority.getName();
        }

        String custom = PriorityDescriptorCache.INSTANCE.descriptorFor(priority).label;
        return StringUtils.isBlank(custom) ? preferencesManager.defaultDescriptorFor(priority).label : custom;
    }

    public static void useCustomPriorityLabels(boolean flag) {

        labelsByPriority.clear();

        for (RulePriority priority : currentPriorities(true)) {
            labelsByPriority.put(priority, pLabelFor(priority, flag));
        }
    }

    public static String labelFor(RulePriority priority) {
        if (labelsByPriority.isEmpty()) {
            useCustomPriorityLabels(preferencesManager.loadPreferences().useCustomPriorityNames());
        }
        return labelsByPriority.get(priority);
    }

    public static Map<Object, ShapeDescriptor> shapesByPriority() {
        Map<Object, ShapeDescriptor> shapesByPriority = new HashMap<>(RulePriority.values().length);
        for (RulePriority priority : RulePriority.values()) {
            shapesByPriority.put(priority, PriorityDescriptorCache.INSTANCE.descriptorFor(priority).shape);
        }
        return shapesByPriority;
    }

    public static List<Integer> getPriorityIntValues() {
        List<Integer> values = new ArrayList<>();
        for (RulePriority priority : RulePriority.values()) {
            values.add(priority.getPriority());
        }
        return values;
    }
}
