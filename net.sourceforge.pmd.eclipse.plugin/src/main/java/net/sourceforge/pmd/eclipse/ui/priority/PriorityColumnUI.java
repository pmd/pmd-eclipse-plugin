/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.priority;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

import net.sourceforge.pmd.eclipse.ui.ItemColumnDescriptor;
import net.sourceforge.pmd.eclipse.ui.ItemFieldAccessor;
import net.sourceforge.pmd.eclipse.ui.ItemFieldAccessorAdapter;
import net.sourceforge.pmd.eclipse.ui.Shape;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.lang.rule.RulePriority;

public final class PriorityColumnUI {
    private PriorityColumnUI() {
        // utility / constants class
    }

    public static final ItemFieldAccessor<String, RulePriority> NAME_ACC = new ItemFieldAccessorAdapter<String, RulePriority>(
            null) {
        @Override
        public String valueFor(RulePriority priority) {
            return PriorityDescriptorCache.INSTANCE.descriptorFor(priority).label;
        }
    };

    public static final ItemFieldAccessor<String, RulePriority> PMD_NAME_ACC = new ItemFieldAccessorAdapter<String, RulePriority>(
            null) {
        @Override
        public String valueFor(RulePriority priority) {
            return priority.getName();
        }
    };

    public static final ItemFieldAccessor<String, RulePriority> DESCRIPTION_ACC = new ItemFieldAccessorAdapter<String, RulePriority>(
            null) {
        @Override
        public String valueFor(RulePriority priority) {
            return PriorityDescriptorCache.INSTANCE.descriptorFor(priority).description;
        }
    };

    public static final ItemFieldAccessor<Shape, RulePriority> SHAPE_ACC = new ItemFieldAccessorAdapter<Shape, RulePriority>(
            null) {
        @Override
        public Shape valueFor(RulePriority priority) {
            return PriorityDescriptorCache.INSTANCE.descriptorFor(priority).shape.shape;
        }
    };

    public static final ItemFieldAccessor<RGB, RulePriority> COLOR_ACC = new ItemFieldAccessorAdapter<RGB, RulePriority>(
            null) {
        @Override
        public RGB valueFor(RulePriority priority) {
            return PriorityDescriptorCache.INSTANCE.descriptorFor(priority).shape.rgbColor;
        }
    };

    public static final ItemFieldAccessor<Integer, RulePriority> SIZE_ACC = new ItemFieldAccessorAdapter<Integer, RulePriority>(
            null) {
        @Override
        public Integer valueFor(RulePriority priority) {
            return PriorityDescriptorCache.INSTANCE.descriptorFor(priority).shape.size;
        }
    };

    public static final ItemFieldAccessor<Integer, RulePriority> VALUE_ACC = new ItemFieldAccessorAdapter<Integer, RulePriority>(
            null) {
        @Override
        public Integer valueFor(RulePriority priority) {
            return priority.getPriority();
        }
    };

    public static final ItemFieldAccessor<Image, RulePriority> IMAGE_ACC = new ItemFieldAccessorAdapter<Image, RulePriority>(
            null) {
        @Override
        public Image imageFor(RulePriority priority) {
            // Note: Not using the cached annotation image, but create a new
            // image based on the
            // current priority descriptor settings.
            return PriorityDescriptorCache.INSTANCE.descriptorFor(priority).createImage();
        }
    };

    public static final ItemColumnDescriptor<String, RulePriority> NAME = new ItemColumnDescriptor<>("",
            StringKeys.PRIORITY_COLUMN_NAME, SWT.LEFT, 25, true, NAME_ACC);
    public static final ItemColumnDescriptor<String, RulePriority> PMD_NAME = new ItemColumnDescriptor<>("",
            StringKeys.PRIORITY_COLUMN_PMD_NAME, SWT.LEFT, 25, true, PMD_NAME_ACC);
    public static final ItemColumnDescriptor<Integer, RulePriority> VALUE = new ItemColumnDescriptor<>("",
            StringKeys.PRIORITY_COLUMN_VALUE, SWT.CENTER, 25, true, VALUE_ACC);
    // PriorityColumnDescriptor size = new PriorityColumnDescriptor("",
    // StringKeys.PRIORITY_COLUMN_SIZE, SWT.RIGHT, 25,
    // true, sizeAcc);
    public static final ItemColumnDescriptor<Image, RulePriority> IMAGE = new ItemColumnDescriptor<>("",
            StringKeys.PRIORITY_COLUMN_SYMBOL, SWT.CENTER, 25, true, IMAGE_ACC).disposeImage();
    // PriorityColumnDescriptor color = new PriorityColumnDescriptor("",
    // StringKeys.PRIORITY_COLUMN_COLOR, SWT.RIGHT,
    // 25, true, colorAcc);
    // PriorityColumnDescriptor description = new PriorityColumnDescriptor("",
    // StringKeys.PRIORITY_COLUMN_DESC,
    // SWT.LEFT, 25, true, descriptionAcc);

    public static final ItemColumnDescriptor[] VISIBLE_COLUMNS = new ItemColumnDescriptor[] { IMAGE, VALUE, NAME,
        PMD_NAME }; // , description

}
