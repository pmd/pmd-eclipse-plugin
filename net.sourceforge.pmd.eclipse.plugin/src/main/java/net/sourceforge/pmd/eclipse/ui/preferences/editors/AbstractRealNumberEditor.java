/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import net.sourceforge.pmd.properties.PropertyDescriptor;
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
                                    PropertyDescriptor<T> numDesc) {

        Spinner spinner = newSpinnerFor(parent, DIGITS);
        //TODO: currently it is not possible to determine the numeric constraints values
        //int min = (int) (numDesc.lowerLimit().doubleValue() * SCALE);
        //int max = (int) (numDesc.upperLimit().doubleValue() * SCALE);
        int min = (int) (DEFAULT_MINIMUM * SCALE);
        int max = (int) (DEFAULT_MAXIMUM * SCALE);
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
