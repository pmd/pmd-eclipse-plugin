package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import net.sourceforge.pmd.NumericPropertyDescriptor;
import net.sourceforge.pmd.PropertySource;

/**
 * @author Brian Remedios
 */
public abstract class AbstractRealNumberEditor<T extends Number> extends AbstractNumericEditorFactory<T> {

    protected static final int digits = 3;

    protected static final double scale = Math.pow(10, digits);


    protected AbstractRealNumberEditor() {
    }


    protected final Spinner newSpinnerFor(Composite parent, PropertySource source,
                                    NumericPropertyDescriptor<T> numDesc) {

        Spinner spinner = newSpinnerFor(parent, digits);
        int min = (int) (numDesc.lowerLimit().doubleValue() * scale);
        int max = (int) (numDesc.upperLimit().doubleValue() * scale);
        spinner.setMinimum(min);
        spinner.setMaximum(max);

        Number value = valueFor(source, numDesc);
        if (value != null) {
            int intVal = (int) (value.doubleValue() * scale);
            spinner.setSelection(intVal);
        }

        return spinner;
    }

}
