/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.util.HashMap;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * @author Brian Remedios
 * @deprecated This editor factory will be removed without replacement. This was only used for supporting the UI
 *             of the plugin and is considered internal API now.
 */
@Deprecated // for removal
public final class EnumerationEditorFactory extends AbstractEditorFactory<Object> {

    public static final EnumerationEditorFactory INSTANCE = new EnumerationEditorFactory();


    private EnumerationEditorFactory() { }


    @Override
    protected Object valueFrom(Control valueControl) {
        // TODO int index = ((Combo) valueControl).getSelectionIndex();
        return null;    // TODO ???
    }


    @Override
    public PropertyDescriptor<Object> createDescriptor(String name, String optionalDescription, Control[] otherData) {
        return PropertyFactory.enumProperty(name, new HashMap<>()).desc("Value set " + name).build();
    }


    @Override
    public Control newEditorOn(Composite parent, final PropertyDescriptor<Object> desc, final PropertySource source,
                               final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final Combo combo = new Combo(parent, SWT.READ_ONLY);

        Object value = valueFor(source, desc);
        Object[][] choices = choices(desc);
        int selectionIdx = indexOf(value, choices);
        if (selectionIdx == -1) {
            Object[][] newChoices = new Object[choices.length + 1][2];
            newChoices[0] = new Object[]{ value.toString(), value };
            for (int i = 0; i < choices.length; i++) {
                newChoices[i + 1] = choices[i];
            }
            choices = newChoices;
            selectionIdx = 0;
        }

        combo.setData(choices);
        combo.setItems(SWTUtil.labelsIn(choices, 0));
        if (selectionIdx >= 0) {
            combo.select(selectionIdx);
        }

        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int selectionIdx = combo.getSelectionIndex();
                Object[][] choices = (Object[][]) combo.getData();
                Object newValue = choices[selectionIdx][1];
                if (Objects.equals(newValue, valueFor(source, desc))) {
                    return;
                }

                source.setProperty(desc, newValue);
                listener.changed(source, desc, newValue);
                adjustRendering(source, desc, combo);
            }
        });

        return combo;
    }


    /**
     * Search through both columns if necessary.
     */
    public static int indexOf(Object item, Object[][] items) {
        int index = indexOf(item, items, 0);
        return index < 0 ? indexOf(item, items, 1) : index;
    }


    public static int indexOf(Object item, Object[][] items, int testColumnIndex) {
        for (int i = 0; i < items.length; i++) {
            if (items[i][testColumnIndex].equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public static <T> Object[][] choices(PropertyDescriptor<T> prop) {
        // TODO: prop.mapping() would be needed to get the valid choices....
        return new Object[0][2];
    }
}
