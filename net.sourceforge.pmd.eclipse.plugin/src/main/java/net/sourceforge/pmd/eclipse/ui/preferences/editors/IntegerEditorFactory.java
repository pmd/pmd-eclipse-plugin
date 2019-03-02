/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.properties.IntegerProperty;
import net.sourceforge.pmd.properties.NumericPropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * @author Brian Remedios
 */
public class IntegerEditorFactory extends AbstractNumericEditorFactory<Integer> {

    public static final IntegerEditorFactory INSTANCE = new IntegerEditorFactory();


    private IntegerEditorFactory() { }


    public PropertyDescriptor<Integer> createDescriptor(String name, String description, Control[] otherData) {

        return new IntegerProperty(
            name,
            description,
            minimumIn(otherData).intValue(),
            maximumIn(otherData).intValue(),
            defaultIn(otherData).intValue(),
            0.0f
        );
    }


    protected Integer valueFrom(Control valueControl) {
        return ((Spinner) valueControl).getSelection();
    }


    public Control newEditorOn(Composite parent, final PropertyDescriptor<Integer> desc, final PropertySource source,
                               final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final Spinner spinner = newSpinner(parent, (NumericPropertyDescriptor<Integer>) desc, valueFor(source, desc));

        spinner.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                Integer newValue = spinner.getSelection();
                if (newValue.equals(valueFor(source, desc))) {
                    return;
                }

                source.setProperty(desc, newValue);
                listener.changed(source, desc, newValue);
                adjustRendering(source, desc, spinner);
            }
        });

        return spinner;
    }


    public static Spinner newSpinner(Composite parent, NumericPropertyDescriptor<?> desc, Object valueIn) {

        Spinner spinner = newSpinnerFor(parent, 0);

        spinner.setMinimum(desc.lowerLimit().intValue());
        spinner.setMaximum(desc.upperLimit().intValue());

        int value = valueIn == null ? spinner.getMinimum() : ((Number) valueIn).intValue();
        spinner.setSelection(value);
        return spinner;
    }

}
