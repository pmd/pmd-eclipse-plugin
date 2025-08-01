/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

/**
 *
 * @author Brian Remedios
 * @deprecated This is internal API and will be removed.
 */
@Deprecated // for removal
public final class SWTUtil {

    private static PMDPlugin plugin = PMDPlugin.getDefault();

    private SWTUtil() {
    }

    /**
     * @deprecated use {@link PMDPlugin#logInformation(String)} instead.
     */
    @Deprecated // for removal
    public static void logInfo(String message) {
        plugin.logInformation(message);
    }

    /**
     * @deprecated use {@link PMDPlugin#logError(org.eclipse.core.runtime.IStatus)} instead.
     */
    @Deprecated // for removal
    public static void logError(String message, Throwable error) {
        plugin.logError(message, error);
    }

    /**
     * Let the buttons operate as a radio group, with only one button selected at a time.
     * 
     * @param buttons
     * @deprecated this is not used and will be removed
     */
    @Deprecated // for removal
    public static void asRadioButtons(final Collection<Button> buttons) {

        Listener listener = new Listener() {
            @Override
            public void handleEvent(Event e) {
                for (Button button : buttons) {
                    if (Objects.equals(e.widget, button)) {
                        button.setSelection(false);
                    }
                }
                ((Button) e.widget).setSelection(true);
            }
        };

        for (Button button : buttons) {
            button.addListener(SWT.Selection, listener);
        }
    }

    /**
     * @deprecated this is not used and will be removed
     */
    @Deprecated // for removal
    public static Set<String> asStringSet(String input, char separator) {
        List<String> values = Arrays.asList(input.split("" + separator));
        return new HashSet<>(values);
    }

    /**
     * @deprecated this is not used and will be removed
     */
    @Deprecated // for removal
    public static String asString(Collection<String> values, char separator) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        String[] strings = values.toArray(new String[0]);
        StringBuilder sb = new StringBuilder(strings[0]);

        for (int i = 1; i < strings.length; i++) {
            sb.append(separator).append(strings[i]);
        }
        return sb.toString();
    }

    /**
     * @deprecated this is not used and will be removed
     */
    @Deprecated // for removal
    public static void setEnabled(Control[] controls, boolean state) {
        net.sourceforge.pmd.eclipse.util.internal.SWTUtil.setEnabled(Arrays.asList(controls), state);
    }

    /**
     * @deprecated This is internal API and will be removed.
     */
    @Deprecated // for removal
    public static void setEnabledRecursive(Control[] controls, boolean state) {
        net.sourceforge.pmd.eclipse.util.internal.SWTUtil.setEnabledRecursive(controls, state);
    }

    /**
     * @deprecated This is internal API and will be removed.
     */
    @Deprecated // for removal
    public static void setEnabled(Control control, boolean flag) {
        net.sourceforge.pmd.eclipse.util.internal.SWTUtil.setEnabled(control, flag);
    }

    /**
     * @deprecated This is internal API and will be removed.
     */
    @Deprecated // for removal
    public static void setEnabled(Collection<Control> controls, boolean flag) {
        net.sourceforge.pmd.eclipse.util.internal.SWTUtil.setEnabled(controls, flag);
    }

    /**
     * @deprecated Use {@link PMDPlugin#getStringTable()} instead.
     */
    @Deprecated // for removal
    public static String stringFor(String key) {
        return net.sourceforge.pmd.eclipse.util.internal.SWTUtil.stringFor(key);
    }

    /**
     * @deprecated Use {@link PMDPlugin#getStringTable()} with suffix {@code .tooltip} instead.
     */
    @Deprecated // for removal
    public static String tooltipFor(String key) {
        return net.sourceforge.pmd.eclipse.util.internal.SWTUtil.tooltipFor(key);
    }

    /**
     * @deprecated this is not used and will be removed
     */
    @Deprecated // for removal
    public static void releaseListeners(Control control, int listenerType) {
        Listener[] listeners = control.getListeners(listenerType);
        for (Listener listener : listeners) {
            control.removeListener(listenerType, listener);
        }
    }

    /**
     * @deprecated This is internal API and will be removed.
     */
    @Deprecated // for removal
    public static String[] labelsIn(Object[][] items, int columnIndex) {
        return net.sourceforge.pmd.eclipse.util.internal.SWTUtil.labelsIn(items, columnIndex);
    }

    /**
     * @deprecated This is internal API and will be removed.
     */
    @Deprecated // for removal
    public static String[] i18lLabelsIn(Object[][] items, int columnIndex) {
        return net.sourceforge.pmd.eclipse.util.internal.SWTUtil.i18lLabelsIn(items, columnIndex);
    }

    /**
     * @deprecated This is internal API and will be removed.
     */
    @Deprecated // for removal
    public static void deselectAll(Combo combo) {
        net.sourceforge.pmd.eclipse.util.internal.SWTUtil.deselectAll(combo);
    }
}
