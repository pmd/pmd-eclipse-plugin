package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.pmd.NumericPropertyDescriptor;
import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.PropertySource;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.lang.rule.properties.IntegerMultiProperty;
import net.sourceforge.pmd.lang.rule.properties.wrappers.PropertyDescriptorWrapper;
import net.sourceforge.pmd.util.CollectionUtil;
import net.sourceforge.pmd.util.NumericConstants;

/**
 * Behaviour: Provide a set of widgets that allows the user to pick a range of integer values. The selected values can
 * only exist once.
 *
 * Provide a spin box for each value selected while ensuring that their choices only contain unselected values. If the
 * last spin box holds the only remaining choice then ensure it gets disabled, the user can only delete it or the
 * previous ones. If the user deletes a previous one then re-enable the last one and add the deleted value to its set of
 * choices.
 *
 * @author Brian Remedios
 */
public class MultiIntegerEditorFactory extends AbstractMultiValueEditorFactory<Integer> {

    public static final MultiIntegerEditorFactory instance = new MultiIntegerEditorFactory();


    private MultiIntegerEditorFactory() { }


    public PropertyDescriptor<List<Integer>> createDescriptor(String name, String optionalDescription,
                                                              Control[] otherData) {

        return new IntegerMultiProperty(name, "Integer values "
            + name, NumericConstants.ZERO, 10, new Integer[] {NumericConstants.ZERO}, 0.0f);
    }


    protected List<Integer> valueFrom(Control valueControl) {
        return currentIntegers((Text) valueControl);
    }


    private List<Integer> currentIntegers(Text textWidget) {

        List<String> numberStrings = textWidgetValues(textWidget);
        if (numberStrings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> ints = new ArrayList<Integer>(numberStrings.size());

        for (String numString : numberStrings) {
            try {
                Integer integer = Integer.parseInt(numString);
                ints.add(integer);
            } catch (Exception e) {
                // just eat it for now
            }
        }
        return ints;
    }


    protected Control addWidget(Composite parent, Object value, PropertyDescriptor<List<Integer>> desc,
                                PropertySource source) {

        NumericPropertyDescriptor<List<Integer>> ip = numericPropertyFrom(desc);   // TODO - do I really have to do this?
        return IntegerEditorFactory.newSpinner(parent, ip, value);
    }


    private static NumericPropertyDescriptor<List<Integer>> numericPropertyFrom(PropertyDescriptor<List<Integer>> desc) {

        if (desc instanceof PropertyDescriptorWrapper<?>) {
            return (NumericPropertyDescriptor<List<Integer>>) ((PropertyDescriptorWrapper<List<Integer>>) desc).getPropertyDescriptor();
        } else {
            return (NumericPropertyDescriptor<List<Integer>>) desc;
        }
    }


    protected void setValue(Control widget, Object valueIn) {

        Spinner spinner = (Spinner) widget;
        int value = valueIn == null ? spinner.getMinimum() : ((Number) valueIn).intValue();
        spinner.setSelection(value);
    }


    protected void configure(final Text textWidget, final PropertyDescriptor<?> desc, final PropertySource source, final ValueChangeListener listener) {

        final IntegerMultiProperty imp = (IntegerMultiProperty) numericPropertyFrom(desc);

        textWidget.addListener(SWT.FocusOut, new Listener() {
            public void handleEvent(Event event) {
                Integer[] newValue = currentIntegers(textWidget);
                Integer[] existingValue = (Integer[]) valueFor(source, imp);
                if (CollectionUtil.areSemanticEquals(existingValue, newValue)) {
                    return;
                }

                source.setProperty(imp, newValue);
                fillWidget(textWidget, desc, source);   // display the accepted values
                listener.changed(source, desc, newValue);
            }
        });
    }


    protected void update(PropertySource source, PropertyDescriptor<?> desc, List<Object> newValues) {
        source.setProperty((IntegerMultiProperty) desc, newValues.toArray(new Integer[newValues.size()]));
    }


    @Override
    protected Object addValueIn(Control widget, PropertyDescriptor<?> desc, PropertySource source) {

        Integer newValue = Integer.valueOf(((Spinner) widget).getSelection());

        Integer[] currentValues = (Integer[]) valueFor(source, desc);
        Integer[] newValues = CollectionUtil.addWithoutDuplicates(currentValues, newValue);
        if (currentValues.length == newValues.length) {
            return null;
        }

        source.setProperty((IntegerMultiProperty) desc, newValues);
        return newValue;
    }
}
