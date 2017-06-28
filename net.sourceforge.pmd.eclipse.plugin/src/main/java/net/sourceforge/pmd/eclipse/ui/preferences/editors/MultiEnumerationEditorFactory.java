package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.PropertySource;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.lang.rule.properties.EnumeratedMultiProperty;
import net.sourceforge.pmd.lang.rule.properties.wrappers.PropertyDescriptorWrapper;
import net.sourceforge.pmd.util.CollectionUtil;

/**
 * Behaviour: Provide a set of widgets that allows the user to select any number of items from a fixed set of choices.
 * Each item can only appear once.
 *
 * Provide a combo box for each value selected while ensuring that their choices only contain unselected values. If the
 * last combo box holds the only remaining choice then ensure it gets disabled, the user can only delete it or the
 * previous ones. If the user deletes a previous one then re-enable the last one and add the deleted value to its set of
 * choices.
 *
 * @author Brian Remedios
 *
 *         ! PLACEHOLDER ONLY - NOT FINISHED YET !
 */
public class MultiEnumerationEditorFactory extends AbstractMultiValueEditorFactory<Object> {

    public static final MultiEnumerationEditorFactory instance = new MultiEnumerationEditorFactory();


    private MultiEnumerationEditorFactory() { }


    @Override
    protected Object addValueIn(Control widget, PropertyDescriptor<List<Object>> desc, PropertySource source) {

        int idx = ((Combo) widget).getSelectionIndex();
        if (idx < 0) {
            return null;
        }

        String newValue = ((Combo) widget).getItem(idx);

        List<Object> currentValues = valueFor(source, desc);
        int nAdded = CollectionUtil.addWithoutDuplicates(Collections.singleton((Object) newValue), currentValues);
        return nAdded == 0 ? null : newValue;
    }


    /**
     * Only add a new widget row if there are any remaining choices to make
     */
    @Override
    protected boolean canAddNewRowFor(PropertyDescriptor<List<Object>> desc, PropertySource source) {

        EnumeratedMultiProperty<?> multi = enumerationPropertyFrom(desc);

        Object[] choices = multi.choices();
        List<Object> values = source.getProperty(desc);

        return choices.length > values.size();
    }


    private static EnumeratedMultiProperty<?> enumerationPropertyFrom(PropertyDescriptor<?> desc) {

        if (desc instanceof PropertyDescriptorWrapper<?>) {
            return (EnumeratedMultiProperty<?>) ((PropertyDescriptorWrapper<?>) desc).getPropertyDescriptor();
        } else {
            return (EnumeratedMultiProperty<?>) desc;
        }
    }


    @Override
    protected Control addWidget(Composite parent, Object value, PropertyDescriptor<List<Object>> desc, final PropertySource source) {

        final Combo combo = new Combo(parent, SWT.READ_ONLY);

        final EnumeratedMultiProperty<?> ep = enumerationPropertyFrom(desc);

        // TODO remove all choices already chosen by previous widgets
        combo.setItems(SWTUtil.labelsIn(ep.choices(), 0));
        int selectionIdx = EnumerationEditorFactory.indexOf(value, ep.choices());
        if (selectionIdx >= 0) {
            combo.select(selectionIdx);
        }

        return combo;
    }


    @Override
    protected void setValue(Control widget, Object value) {
        // not necessary, set in addWidget method?
    }


    @Override
    protected void update(PropertySource source, PropertyDescriptor<List<Object>> desc, List<Object> newValues) {
        source.setProperty(desc, newValues);
    }


    @Override
    protected List<Object> valueFrom(Control valueControl) {            // unreferenced method?
        return null;
    }


    public PropertyDescriptor<List<Object>> createDescriptor(String name, String description, Control[] otherData) {
        return null;
    }


    @Override
    protected void configure(Text text, PropertyDescriptor<List<Object>> desc, PropertySource source, ValueChangeListener listener) {
        text.setEditable(false);
    }


}
