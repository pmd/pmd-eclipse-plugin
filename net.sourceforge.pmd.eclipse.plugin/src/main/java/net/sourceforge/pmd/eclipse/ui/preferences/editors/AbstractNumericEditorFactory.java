/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import static net.sourceforge.pmd.eclipse.util.internal.SWTUtil.stringFor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.properties.PropertyDescriptor;

/**
 * @author Brian Remedios
 * @deprecated This editor factory will be removed without replacement. This was only used for supporting the UI
 *             of the plugin and is considered internal API now.
 */
@Deprecated // for removal
public abstract class AbstractNumericEditorFactory<T> extends AbstractEditorFactory<T> {

    public static final int DEFAULT_MINIMUM = 0;
    public static final int DEFAULT_MAXIMUM = 1000;


    protected AbstractNumericEditorFactory() {
        // protected default constructor for subclassing
    }


    public Control[] createOtherControlsOn(Composite parent, PropertyDescriptor<T> desc, Rule rule,
                                           ValueChangeListener listener, SizeChangeListener sizeListener) {

        Label defaultLabel = newLabel(parent, stringFor(StringKeys.RULEEDIT_LABEL_DEFAULT));
        Control valueControl = newEditorOn(parent, desc, rule, listener, sizeListener);

        Label minLabel = newLabel(parent, stringFor(StringKeys.RULEEDIT_LABEL_MIN));
        Spinner minWidget = newSpinnerFor(parent, digitPrecision());
        Label maxLabel = newLabel(parent, stringFor(StringKeys.RULEEDIT_LABEL_MAX));
        Spinner maxWidget = newSpinnerFor(parent, digitPrecision());

        linkup(minWidget, (Spinner) valueControl, maxWidget);

        return new Control[] {
            defaultLabel, valueControl,
            minLabel, minWidget,
            maxLabel, maxWidget,
        };
    }


    protected static Spinner newSpinnerFor(Composite parent, int digits) {

        Spinner spinner = new Spinner(parent, SWT.BORDER);
        spinner.setDigits(digits);
        return spinner;
    }


    protected int digitPrecision() {
        return 0;
    }


    private void linkup(final Spinner minWidget, final Spinner valueWidget, final Spinner maxWidget) {
        minWidget.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                adjustForMin(minWidget, valueWidget, maxWidget);
            }
        });

        valueWidget.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                adjustForValue(minWidget, valueWidget, maxWidget);
            }
        });

        maxWidget.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                adjustForMax(minWidget, valueWidget, maxWidget);
            }
        });
    }


    private void adjustForMin(Spinner minWidget, Spinner valueWidget, Spinner maxWidget) {
        int min = minWidget.getSelection();
        if (valueWidget.getSelection() < min) {
            valueWidget.setSelection(min);
        }
        if (maxWidget.getSelection() < min) {
            maxWidget.setSelection(min);
        }
    }


    private void adjustForValue(Spinner minWidget, Spinner valueWidget, Spinner maxWidget) {
        int value = valueWidget.getSelection();
        if (minWidget.getSelection() > value) {
            minWidget.setSelection(value);
        }
        if (maxWidget.getSelection() < value) {
            maxWidget.setSelection(value);
        }
    }


    private void adjustForMax(Spinner minWidget, Spinner valueWidget, Spinner maxWidget) {
        int max = maxWidget.getSelection();
        if (valueWidget.getSelection() > max) {
            valueWidget.setSelection(max);
        }
        if (minWidget.getSelection() > max) {
            minWidget.setSelection(max);
        }
    }


    protected Number defaultIn(Control[] controls) {

        return controls == null
                ? Integer.valueOf(DEFAULT_MINIMUM)
                : (Number) valueFrom(controls[1]);
    }


    protected Number minimumIn(Control[] controls) {

        return controls == null
                ? Integer.valueOf(0)
                : (Number) valueFrom(controls[3]);
    }


    protected Number maximumIn(Control[] controls) {

        return controls == null
                ? Integer.valueOf(DEFAULT_MAXIMUM)
                : (Number) valueFrom(controls[5]);
    }
}
