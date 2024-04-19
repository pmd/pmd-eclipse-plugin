/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.util.internal;

import java.util.Collection;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

public final class SWTUtil {
    public static final String TOOLTIP_SUFFIX = ".tooltip";

    private SWTUtil() {
        // utility class
    }

    public static String stringFor(String key) {
        return PMDPlugin.getDefault().getStringTable().getString(key);
    }

    public static String tooltipFor(String key) {
        String ttKey = key + TOOLTIP_SUFFIX;
        String tooltip = stringFor(ttKey);
        return ttKey.equals(tooltip) ? stringFor(key) : tooltip;
    }


    public static String[] labelsIn(Object[][] items, int columnIndex) {
        String[] labels = new String[items.length];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = items[i][columnIndex].toString();
        }
        return labels;
    }

    public static String[] i18lLabelsIn(Object[][] items, int columnIndex) {
        String[] labels = labelsIn(items, columnIndex);
        String xlation;

        for (int i = 0; i < labels.length; i++) {
            xlation = stringFor(labels[i]);
            labels[i] = xlation == null ? labels[i] : xlation;
        }
        return labels;
    }

    public static void deselectAll(Combo combo) {
        int count = combo.getItems().length;
        for (int i = 0; i < count; i++) {
            combo.deselect(i);
        }
    }

    public static void setEnabled(Control control, boolean flag) {
        if (control == null || control.isDisposed()) {
            return;
        }
        control.setEnabled(flag);
    }

    public static void setEnabled(Collection<Control> controls, boolean flag) {
        for (Control control : controls) {
            setEnabled(control, flag);
        }
    }

    public static void setEnabledRecursive(Control[] controls, boolean state) {
        for (Control control : controls) {
            if (control instanceof Composite) {
                setEnabledRecursive(((Composite) control).getChildren(), state);
            }
            setEnabled(control, state);
        }
    }
}
