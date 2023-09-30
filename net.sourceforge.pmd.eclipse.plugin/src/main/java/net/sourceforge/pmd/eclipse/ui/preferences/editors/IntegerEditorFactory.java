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
import net.sourceforge.pmd.properties.NumericConstraints;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * @author Brian Remedios
 */
public final class IntegerEditorFactory extends AbstractNumericEditorFactory<Integer> {

    public static final IntegerEditorFactory INSTANCE = new IntegerEditorFactory();


    private IntegerEditorFactory() { }


    @Override
    public PropertyDescriptor<Integer> createDescriptor(String name, String description, Control[] otherData) {
        return PropertyFactory.intProperty(name).desc(description)
            .require(NumericConstraints.inRange(minimumIn(otherData).intValue(), maximumIn(otherData).intValue()))
            .defaultValue(defaultIn(otherData).intValue()).build();
    }

    @Override
    protected Integer valueFrom(Control valueControl) {
        return ((Spinner) valueControl).getSelection();
    }

    @Override
    public Control newEditorOn(Composite parent, final PropertyDescriptor<Integer> desc, final PropertySource source,
                               final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final Spinner spinner = newSpinner(parent, desc, valueFor(source, desc));

        spinner.addModifyListener(new ModifyListener() {
            @Override
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


    public static Spinner newSpinner(Composite parent, PropertyDescriptor<?> desc, Object valueIn) {

        Spinner spinner = newSpinnerFor(parent, 0);

        //TODO: currently it is not possible to determine the numeric constraints values
        //spinner.setMinimum(desc.lowerLimit().intValue());
        //spinner.setMaximum(desc.upperLimit().intValue());
        spinner.setMinimum(DEFAULT_MINIMUM);
        spinner.setMaximum(DEFAULT_MAXIMUM);

        int value = valueIn == null ? spinner.getMinimum() : ((Number) valueIn).intValue();
        spinner.setSelection(value);
        return spinner;
    }

}
