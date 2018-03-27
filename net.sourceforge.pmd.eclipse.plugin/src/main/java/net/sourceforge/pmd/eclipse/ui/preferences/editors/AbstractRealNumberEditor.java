package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import net.sourceforge.pmd.properties.NumericPropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * @author Brian Remedios
 */
public abstract class AbstractRealNumberEditor<T extends Number> extends AbstractNumericEditorFactory<T> {

    protected static final int DIGITS = 3;

    protected static final double SCALE = Math.pow(10, DIGITS);


    protected AbstractRealNumberEditor() {
    }


    protected final Spinner newSpinnerFor(Composite parent, PropertySource source,
                                    NumericPropertyDescriptor<T> numDesc) {

        Spinner spinner = newSpinnerFor(parent, DIGITS);
        int min = (int) (numDesc.lowerLimit().doubleValue() * SCALE);
        int max = (int) (numDesc.upperLimit().doubleValue() * SCALE);
        spinner.setMinimum(min);
        spinner.setMaximum(max);

        Number value = valueFor(source, numDesc);
        if (value != null) {
            int intVal = (int) (value.doubleValue() * SCALE);
            spinner.setSelection(intVal);
        }

        return spinner;
    }

}
