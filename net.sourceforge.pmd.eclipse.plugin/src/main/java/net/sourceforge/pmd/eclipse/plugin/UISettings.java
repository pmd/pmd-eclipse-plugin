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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferencesManager;
import net.sourceforge.pmd.eclipse.ui.Shape;
import net.sourceforge.pmd.eclipse.ui.ShapeDescriptor;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.nls.StringTable;
import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptor;
import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptorCache;
import net.sourceforge.pmd.eclipse.util.FontBuilder;

/**
 * 
 * @author Brian Remedios
 */
public class UISettings {
    private UISettings() {
        // utility class
    }

    private static String[] priorityLabels;

    private static Map<RulePriority, String> labelsByPriority = new HashMap<RulePriority, String>();

    private static IPreferencesManager preferencesManager = PMDPlugin.getDefault().getPreferencesManager();

    public static final FontBuilder CODE_FONT_BUILDER = new FontBuilder("Courier", 11, SWT.NORMAL);

    @Deprecated
    public static void reloadPriorities() {
        // no-op - nothing to be done anymore. The priority descriptor are cached by PriorityDescriptorCache and not here.
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

        Map<Shape, ShapeDescriptor> shapes = new HashMap<Shape, ShapeDescriptor>();

        for (Shape shape : EnumSet.allOf(Shape.class)) {
            shapes.put(shape, new ShapeDescriptor(shape, color, size));
        }

        return shapes;
    }

    /**
     * @deprecated Use {@link PriorityDescriptorCache#descriptorFor(RulePriority)} to retrieve the {@link PriorityDescriptor}.
     */
    @Deprecated
    public static String markerFilenameFor(RulePriority priority) {
        String fileDir = PMDPlugin.getPluginFolder().getAbsolutePath();
        return fileDir + "/" + relativeMarkerFilenameFor(priority);
    }

    /**
     * @deprecated Use {@link PriorityDescriptorCache#descriptorFor(RulePriority)} to retrieve the {@link PriorityDescriptor}.
     */
    @Deprecated
    public static String relativeMarkerFilenameFor(RulePriority priority) {
        return "icons/markerP" + priority.getPriority() + ".png";
    }

    /**
     * @deprecated Use {@link PriorityDescriptorCache#descriptorFor(RulePriority)} to retrieve the {@link PriorityDescriptor}.
     */
    @Deprecated
    public static ImageDescriptor markerDescriptorFor(RulePriority priority) {
        PriorityDescriptor pd = PriorityDescriptorCache.INSTANCE.descriptorFor(priority);
        return pd.getAnnotationImageDescriptor();
    }

    /**
     * @deprecated Use {@link PriorityDescriptorCache#descriptorFor(RulePriority)} to retrieve the {@link PriorityDescriptor}.
     */
    @Deprecated
    public static Map<Integer, ImageDescriptor> markerImgDescriptorsByPriority() {
        RulePriority[] priorities = currentPriorities(true);
        Map<Integer, ImageDescriptor> overlaysByPriority = new HashMap<Integer, ImageDescriptor>(priorities.length);
        for (RulePriority priority : priorities) {
            overlaysByPriority.put(priority.getPriority(), markerDescriptorFor(priority));
        }
        return overlaysByPriority;
    }

    /**
     * Marker Icons are not stored to files anymore. They are generated on the fly and cached by {@link PriorityDescriptorCache}.
     * @deprecated Use {@link PriorityDescriptorCache#descriptorFor(RulePriority)} to retrieve the {@link PriorityDescriptor}.
     */
    @Deprecated
    public static void createRuleMarkerIcons(Display display) {
        ImageLoader loader = new ImageLoader();

        PriorityDescriptorCache pdc = PriorityDescriptorCache.INSTANCE;

        for (RulePriority priority : currentPriorities(true)) {
            Image image = pdc.descriptorFor(priority).getAnnotationImage();
            loader.data = new ImageData[] { image.getImageData() };
            String fullPath = markerFilenameFor(priority);
            PMDPlugin.getDefault().logInformation("Writing marker icon to: " + fullPath);
            loader.save(fullPath, SWT.IMAGE_PNG);
            image.dispose();
        }
    }

    private static String pLabelFor(RulePriority priority, boolean useCustom) {

        if (!useCustom) {
            return priority.getName();
        }

        String custom = descriptorFor(priority).label;
        return StringUtils.isBlank(custom) ? preferencesManager.defaultDescriptorFor(priority).label : custom;
    }

    public static void useCustomPriorityLabels(boolean flag) {

        labelsByPriority.clear();

        for (RulePriority priority : currentPriorities(true)) {
            labelsByPriority.put(priority, pLabelFor(priority, flag));
        }
    }

    /**
     * @deprecated Use {@link PriorityDescriptorCache#descriptorFor(RulePriority)} to retrieve the {@link PriorityDescriptor#description}.
     */
    @Deprecated
    public static String descriptionFor(RulePriority priority) {
        return descriptorFor(priority).description;
    }

    /**
     * @deprecated Use {@link PriorityDescriptorCache#descriptorFor(RulePriority)} to retrieve the {@link PriorityDescriptor}.
     */
    @Deprecated
    public static PriorityDescriptor descriptorFor(RulePriority priority) {
        return PriorityDescriptorCache.INSTANCE.descriptorFor(priority);
    }

    public static String labelFor(RulePriority priority) {
        if (labelsByPriority.isEmpty()) {
            useCustomPriorityLabels(preferencesManager.loadPreferences().useCustomPriorityNames());
        }
        return labelsByPriority.get(priority);
    }

    public static Map<Object, ShapeDescriptor> shapesByPriority() {
        Map<Object, ShapeDescriptor> shapesByPriority = new HashMap<Object, ShapeDescriptor>(RulePriority.values().length);
        for (RulePriority priority : RulePriority.values()) {
            shapesByPriority.put(priority, PriorityDescriptorCache.INSTANCE.descriptorFor(priority).shape);
        }
        return shapesByPriority;
    }

    /**
     * @deprecated Use {@link RulePriority#valueOf(int)} directly.
     */
    @Deprecated
    public static RulePriority priorityFor(int value) {
        return RulePriority.valueOf(value);
    }

    /**
     * Return the priority labels
     * 
     * @deprecated - not referenced in the modern UI
     */
    @Deprecated
    public static String[] getPriorityLabels() {
        if (priorityLabels == null) {
            final StringTable stringTable = PMDPlugin.getDefault().getStringTable();
            priorityLabels = new String[] { stringTable.getString(StringKeys.PRIORITY_ERROR_HIGH),
                stringTable.getString(StringKeys.PRIORITY_ERROR),
                stringTable.getString(StringKeys.PRIORITY_WARNING_HIGH),
                stringTable.getString(StringKeys.PRIORITY_WARNING),
                stringTable.getString(StringKeys.PRIORITY_INFORMATION),
            };
        }

        return priorityLabels; // NOPMD by Herlin on 11/10/06 00:22
    }

    public static List<Integer> getPriorityIntValues() {

        List<Integer> values = new ArrayList<Integer>();
        for (RulePriority priority : RulePriority.values()) {
            values.add(priority.getPriority());
        }
        return values;
    }
}
