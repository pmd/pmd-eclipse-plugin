/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

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

import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.properties.PropertySource;
import net.sourceforge.pmd.properties.constraints.NumericConstraints;
import net.sourceforge.pmd.util.CollectionUtil;

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
public final class MultiIntegerEditorFactory extends AbstractMultiValueEditorFactory<Integer> {

    public static final MultiIntegerEditorFactory INSTANCE = new MultiIntegerEditorFactory();

    private MultiIntegerEditorFactory() { }

    @Override
    public PropertyDescriptor<List<Integer>> createDescriptor(String name, String optionalDescription,
                                                              Control[] otherData) {
        return PropertyFactory.intListProperty(name).desc(optionalDescription)
            .requireEach(NumericConstraints.inRange(0, 10))
            .defaultValues(0).build();
    }

    @Override
    protected List<Integer> valueFrom(Control valueControl) {
        return currentIntegers((Text) valueControl);
    }

    private List<Integer> currentIntegers(Text textWidget) {
        List<String> numberStrings = textWidgetValues(textWidget);
        if (numberStrings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> ints = new ArrayList<>(numberStrings.size());

        for (String numString : numberStrings) {
            try {
                Integer integer = Integer.parseInt(numString);
                ints.add(integer);
            } catch (Exception ignored) {
                // just eat it for now
            }
        }
        return ints;
    }

    @Override
    protected Control addWidget(Composite parent, Integer value, PropertyDescriptor<List<Integer>> desc,
                                PropertySource source) {
        return IntegerEditorFactory.newSpinner(parent, desc, value);
    }

    @Override
    protected void setValue(Control widget, Integer valueIn) {
        Spinner spinner = (Spinner) widget;
        int value = valueIn == null ? spinner.getMinimum() : valueIn;
        spinner.setSelection(value);
    }

    @Override
    protected void configure(final Text textWidget, final PropertyDescriptor<List<Integer>> desc,
                             final PropertySource source, final ValueChangeListener listener) {

        textWidget.addListener(SWT.FocusOut, new Listener() {
            @Override
            public void handleEvent(Event event) {
                List<Integer> newValue = currentIntegers(textWidget);
                List<Integer> existingValue = valueFor(source, desc);
                if (existingValue != null && existingValue.equals(newValue)) {
                    return;
                }

                source.setProperty(desc, newValue);
                fillWidget(textWidget, desc, source);   // display the accepted values
                listener.changed(source, desc, newValue);
            }
        });
    }

    @Override
    protected void update(PropertySource source, PropertyDescriptor<List<Integer>> desc, List<Integer> newValues) {
        source.setProperty(desc, newValues);
    }


    @Override
    protected Integer addValueIn(Control widget, PropertyDescriptor<List<Integer>> desc, PropertySource source) {

        Integer newValue = ((Spinner) widget).getSelection();

        List<Integer> currentValues = valueFor(source, desc);
        int nAdded = CollectionUtil.addWithoutDuplicates(Collections.singleton(newValue), currentValues);
        return nAdded == 0 ? null : newValue;
    }
}
