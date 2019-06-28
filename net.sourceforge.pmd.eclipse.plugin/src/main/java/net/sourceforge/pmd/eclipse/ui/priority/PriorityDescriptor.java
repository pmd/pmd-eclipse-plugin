/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.priority;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.Shape;
import net.sourceforge.pmd.eclipse.ui.ShapeDescriptor;
import net.sourceforge.pmd.eclipse.ui.ShapePainter;
import net.sourceforge.pmd.eclipse.ui.views.actions.AbstractPMDAction;
import net.sourceforge.pmd.util.StringUtil;

/**
 * 
 * @author Brian Remedios
 */
public class PriorityDescriptor implements Cloneable {

    public final RulePriority priority;
    public String label;
    public String description;
    public String filterText;
    public String iconId;
    public ShapeDescriptor shape;

    private static final RGB PROTO_TRANSPARENT_COLOR = new RGB(1, 1, 1); // almost full black, unlikely to be used

    private static final char DELIMITER = '_';

    public PriorityDescriptor(RulePriority thePriority, String theLabelKey, String theFilterTextKey, String theIconId,
            Shape theShape, RGB theColor, int theSize) {
        this(thePriority, theLabelKey, theFilterTextKey, theIconId, new ShapeDescriptor(theShape, theColor, theSize));
    }

    private PriorityDescriptor(RulePriority thePriority) {
        priority = thePriority;
    }

    public PriorityDescriptor(RulePriority thePriority, String theLabelKey, String theFilterTextKey, String theIconId,
            ShapeDescriptor theShape) {
        priority = thePriority;
        label = AbstractPMDAction.getString(theLabelKey);
        description = "--"; // TODO
        filterText = AbstractPMDAction.getString(theFilterTextKey);
        iconId = null;
        if (theIconId != null && !theIconId.isEmpty() && !"null".equals(theIconId)) {
            iconId = theIconId;
        }
        shape = theShape;
    }

    public static PriorityDescriptor from(String text) {

        String[] values = text.split(Character.toString(DELIMITER));
        if (values.length != 7) {
            return null;
        }

        RGB rgb = rgbFrom(values[5]);
        if (rgb == null) {
            return null;
        }

        return new PriorityDescriptor(RulePriority.valueOf(Integer.parseInt(values[0])), values[1], values[2],
                values[3], shapeFrom(values[4]), rgb, Integer.parseInt(values[6]));
    }

    private static Shape shapeFrom(String id) {
        int num = Integer.parseInt(id);
        for (Shape shape : EnumSet.allOf(Shape.class)) {
            if (shape.id == num) {
                return shape;
            }
        }
        return null;
    }

    private static RGB rgbFrom(String desc) {
        String[] clrs = desc.split(",");
        if (clrs.length != 3) {
            return null;
        }
        return new RGB(Integer.parseInt(clrs[0]), Integer.parseInt(clrs[1]), Integer.parseInt(clrs[2]));
    }

    private static void rgbOn(StringBuilder sb, RGB rgb) {
        sb.append(rgb.red).append(',');
        sb.append(rgb.green).append(',');
        sb.append(rgb.blue);
    }

    public String storeString() {
        StringBuilder sb = new StringBuilder();
        storeOn(sb);
        return sb.toString();
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (other.getClass() != getClass()) {
            return false;
        }

        PriorityDescriptor otherOne = (PriorityDescriptor) other;

        return priority.equals(otherOne.priority) && StringUtil.isSame(label, otherOne.label, false, false, false)
                && shape.equals(otherOne.shape)
                && StringUtil.isSame(description, otherOne.description, false, false, false)
                && StringUtil.isSame(filterText, otherOne.filterText, false, false, false)
                && StringUtil.isSame(iconId, otherOne.iconId, false, false, false);
    }

    public int hashCode() {
        return priority.hashCode() ^ shape.hashCode() ^ String.valueOf(label).hashCode()
                ^ String.valueOf(description).hashCode() ^ String.valueOf(iconId).hashCode();
    }

    public void storeOn(StringBuilder sb) {
        sb.append(priority.getPriority()).append(DELIMITER);
        sb.append(label).append(DELIMITER);
        // sb.append(description).append(DELIMITER);
        sb.append(filterText).append(DELIMITER);
        if (iconId != null) {
            sb.append(iconId);
        }
        sb.append(DELIMITER);
        sb.append(shape.shape.id).append(DELIMITER);
        rgbOn(sb, shape.rgbColor);
        sb.append(DELIMITER);
        sb.append(shape.size).append(DELIMITER);
    }

    /**
     * @deprecated use {@link #getAnnotationImageDescriptor()} instead.
     */
    @Deprecated
    public ImageDescriptor getImageDescriptor() {
        return PMDPlugin.getImageDescriptor(iconId);
    }

    public PriorityDescriptor clone() {
        PriorityDescriptor copy = new PriorityDescriptor(priority);
        copy.label = label;
        copy.description = description;
        copy.filterText = filterText;
        copy.iconId = iconId;
        copy.shape = shape.clone();
        return copy;
    }

    /**
     * @deprecated Use {@link #getAnnotationImage()} or {@link #getAnnotationImageDescriptor()} instead.
     */
    @Deprecated
    public Image getImage(Display display) {
        return createImage();
    }

    /**
     * @deprecated Use {@link #getAnnotationImage()} or {@link #getAnnotationImageDescriptor()} instead.
     */
    @Deprecated
    public Image getImage(Display display, int maxDimension) {
        return ShapePainter.newDrawnImage(display, Math.min(shape.size, maxDimension),
                Math.min(shape.size, maxDimension), shape.shape, PROTO_TRANSPARENT_COLOR, shape.rgbColor // fillColour
        );
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PriorityDescriptor: ");
        sb.append(priority).append(", ");
        sb.append(label).append(", ");
        sb.append(description).append(", ");
        sb.append(filterText).append(", ");
        sb.append(iconId).append(", ");
        sb.append(shape);
        return sb.toString();
    }

    private Map<Integer, Image> cachedImages = new HashMap<>(3);
    private static final int ANNOTATION_IMAGE_DIMENSION = 9;

    public Image getAnnotationImage() {
        return getImage(ANNOTATION_IMAGE_DIMENSION);
    }

    public ImageDescriptor getAnnotationImageDescriptor() {
        return ImageDescriptor.createFromImage(getAnnotationImage());
    }

    private Image createImage(final int size) {
        if (iconId == null || iconId.isEmpty() || "null".equals(iconId)) {
            return ShapePainter.newDrawnImage(Display.getCurrent(), size, size, shape.shape, PROTO_TRANSPARENT_COLOR,
                    shape.rgbColor // fillColour
            );
        } else {
            Image srcImage = PriorityDescriptorIcon.getById(iconId).getImage();
            // need to create a copy since the image of the PropertyDescriptor might be disposed
            // if the image is changed. See #refreshImages()
            // also, we might need to scale the icon.
            ImageData imageData = srcImage.getImageData().scaledTo(size, size);
            Image copy = new Image(srcImage.getDevice(), imageData);
            return copy;
        }
    }

    /**
     * Gets the marker image in a specific size. The image is cached and reused.
     * @param size
     * @return
     */
    public Image getImage(int size) {
        Image cached = cachedImages.get(size);
        if (cached == null) {
            cached = createImage(size);
            cachedImages.put(size, cached);
        }
        return cached;
    }

    /**
     * Creates a new image with the current setting. This is needed during the configuration, when
     * the priority descriptor is changed, but not persisted yet. The cached image returned by
     * {@link #getAnnotationImage()} would not reflect that change and a preview is not possible.
     *
     * <p>The image is not cached.
     * @return
     */
    public Image createImage() {
        return createImage(shape.size);
    }

    public void dispose() {
        for (Image image : cachedImages.values()) {
            image.dispose();
        }
        cachedImages.clear();
    }

    /**
     * Eagerly create the images.
     */
    public void refreshImages() {
        dispose();
        Image image = getAnnotationImage();
        if (image == null) {
            throw new RuntimeException("Could not create annotation image");
        }
        image = getImage(16);
        if (image == null) {
            throw new RuntimeException("Could not create marker image");
        }
    }
}
