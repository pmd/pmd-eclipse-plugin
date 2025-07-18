/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Renders a set of coloured shapes mapped to known set of incoming items
 * 
 * @author Brian Remedios
 */
public class ShapePicker<T extends Object> extends Canvas implements ISelectionProvider {

    private static Map<RGB, Color> coloursByRGB = new HashMap<>();

    private T[] items;
    private int itemWidth;
    private int gap = 6;
    private T selectedItem;
    private T highlightItem;
    private Color selectedItemFillColor;
    private LabelProvider tooltipProvider;
    private Map<T, ShapeDescriptor> shapeDescriptorsByItem;
    // private Map<T, String> tooltipsByItem;

    private List<ISelectionChangedListener> listeners;

    public ShapePicker(Composite parent, int style, int theItemWidth) {
        super(parent, style);

        itemWidth = theItemWidth;

        addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent pe) {
                doPaint(pe);
            }
        });

        addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                if (!getEnabled()) {
                    return;
                }
                T newItem = itemAt(e.x);
                if (!Objects.equals(newItem, highlightItem)) {
                    highlightItem = newItem;
                    setToolTipText(tooltipFor(newItem));
                    redraw();
                }
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                forceFocus();
            }

            @Override
            public void mouseUp(MouseEvent e) {
                if (!getEnabled()) {
                    return;
                }
                T newItem = itemAt(e.x);
                if (!Objects.equals(newItem, selectedItem)) {
                    selectedItem = newItem;
                    redraw();
                    selectionChanged();
                }
            }
        });

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                redraw();
            }

            @Override
            public void focusLost(FocusEvent e) {
                redraw();
            }
        });

        selectedItemFillColor = colorFor(new RGB(200, 200, 200));
    }

    private Color colourFor(int itemIndex) {
        ShapeDescriptor desc = shapeDescriptorsByItem.get(items[itemIndex]);
        if (desc == null) {
            return Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
        }

        RGB rgb = desc.rgbColor;

        return colorFor(rgb);
    }

    private String tooltipFor(T item) {
        return item == null ? "" : tooltipProvider == null ? item.toString() : tooltipProvider.labelFor(item);
    }

    private Color colorFor(RGB rgb) {

        Color color = coloursByRGB.get(rgb);
        if (color != null) {
            return color;
        }

        color = new Color(null, rgb.red, rgb.green, rgb.blue);
        coloursByRGB.put(rgb, color);

        return color;
    }

    private void selectionChanged() {
        if (listeners == null) {
            return;
        }
        IStructuredSelection selection = StructuredSelection.EMPTY;
        if (selectedItem != null) {
            selection = new StructuredSelection(new Object[] { selectedItem });
        }
        SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
        for (ISelectionChangedListener listener : listeners) {
            listener.selectionChanged(event);
        }
    }

    @Override
    public boolean forceFocus() {
        boolean state = super.forceFocus();
        redraw();
        return state;
    }

    private Shape shapeFor(int itemIndex) {
        ShapeDescriptor desc = shapeDescriptorsByItem.get(items[itemIndex]);
        return desc == null ? Shape.circle : desc.shape;
    }

    private T itemAt(int xIn) {

        if (items == null) {
            return null;
        }

        int width = getSize().x;
        int xBoundary = 3;

        for (int i = 0; i < items.length; i++) {

            int xOffset = 0;
            int step = (itemWidth + gap) * i;

            switch (SWT.LEFT) { // TODO take from style bits
            case SWT.CENTER:
                xOffset = width / 2 - itemWidth / 2 - xBoundary + step;
                break;
            case SWT.RIGHT:
                xOffset = 0 - xBoundary;
                break;
            case SWT.LEFT:
                xOffset = xBoundary + step;
                break;
            default:
                // TODO
            }

            if (xIn < xOffset) {
                return items[i == 0 ? 0 : i - 1];
            }
            if (xIn < xOffset + itemWidth) {
                return items[i];
            }
        }

        return null;
    }

    private void doPaint(PaintEvent pe) {

        if (items == null) {
            return;
        }

        GC gc = pe.gc;
        int width = getSize().x;
        int xBoundary = 3;

        if (isFocusControl()) {
            gc.drawFocus(0, 0, getSize().x, getSize().y);
        }

        for (int i = 0; i < items.length; i++) {
            gc.setBackground(Objects.equals(selectedItem, items[i]) ? selectedItemFillColor : colourFor(i));

            int xOffset = 0;
            int step = (itemWidth + gap) * i;

            switch (SWT.LEFT) { // TODO take from style bits
            case SWT.CENTER:
                xOffset = width / 2 - itemWidth / 2 - xBoundary + step;
                break;
            case SWT.RIGHT:
                xOffset = 0 - xBoundary;
                break;
            case SWT.LEFT:
                xOffset = xBoundary + step;
                break;
            default:
                //TODO
            }

            gc.setLineWidth(showHighlightOn(items[i]) ? 3 : 1);

            ShapePainter.drawShape(itemWidth, itemWidth, shapeFor(i), gc, pe.x + xOffset, pe.y + gap, null);
        }
    }

    private boolean showHighlightOn(T item) {
        return isFocusControl() && Objects.equals(highlightItem, item);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        Point pt = getSize();
        // TODO adapt by shape count
        return new Point(pt.x, pt.y);
    }

    public void setSelection(T item) {
        selectedItem = item;
        redraw();
        selectionChanged();
    }

    public void tooltipProvider(LabelProvider provider) {
        tooltipProvider = provider;
    }

    @Override
    public ISelection getSelection() {
        return new StructuredSelection(selectedItem);
    }

    public void setGap(int theGap) {
        gap = theGap;
        redraw();
    }

    public void setItems(T[] theItems) {
        items = theItems;
        redraw();
    }

    public void setItemWidth(int width) {
        itemWidth = width;
        redraw();
    }

    public void setShapeMap(Map<T, ShapeDescriptor> theShapeMap) {
        shapeDescriptorsByItem = theShapeMap;
        redraw();
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        if (listeners == null) {
            listeners = new Vector<>();
        }
        listeners.add(listener);
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        if (listeners == null) {
            return;
        }
        listeners.remove(listener);
    }

    @Override
    public void setSelection(ISelection selection) {
        setSelection((T) ((StructuredSelection) selection).getFirstElement());
    }

    public void removeSelection() {
        setSelection((T) null);
    }

    // public void setTooltipMap(Map<T, String> theTooltips) {
    // tooltipsByItem = theTooltips;
    // }
}
