package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.sourceforge.pmd.EnumeratedPropertyDescriptor;
import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.PropertySource;
import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.lang.rule.properties.EnumeratedProperty;

/**
 * @author Brian Remedios
 */
public class EnumerationEditorFactory extends AbstractEditorFactory<Object> {

    public static final EnumerationEditorFactory instance = new EnumerationEditorFactory();


    private EnumerationEditorFactory() { }


    protected Object valueFrom(Control valueControl) {

        int index = ((Combo) valueControl).getSelectionIndex();
        return null;    // TODO ???
    }


    public PropertyDescriptor<Object> createDescriptor(String name, String optionalDescription, Control[] otherData) {
        return new EnumeratedProperty<Object>(name, "Value set " + name, null, null, 0, Object.class, 0.0f);
    }


    public Control newEditorOn(Composite parent, final PropertyDescriptor<Object> desc, final PropertySource source,
                               final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final Combo combo = new Combo(parent, SWT.READ_ONLY);

        final EnumeratedPropertyDescriptor<Object, Object> ep = (EnumeratedPropertyDescriptor<Object, Object>) desc;
        Object value = valueFor(source, desc);
        combo.setItems(SWTUtil.labelsIn(ep.choices(), 0));
        int selectionIdx = indexOf(value, ep.choices());
        if (selectionIdx >= 0) {
            combo.select(selectionIdx);
        }

        combo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int selectionIdx = combo.getSelectionIndex();
                Object newValue = ep.choices()[selectionIdx][1];
                if (newValue == valueFor(source, desc)) {
                    return;
                }

                source.setProperty(ep, newValue);
                listener.changed(source, desc, newValue);
                adjustRendering(source, desc, combo);
            }
        });

        return combo;
    }


    /**
     * Search through both columns if necessary
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
}
