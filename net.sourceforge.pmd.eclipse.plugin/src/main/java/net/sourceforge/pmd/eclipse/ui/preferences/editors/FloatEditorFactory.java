package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.PropertySource;
import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.lang.rule.properties.FloatProperty;
import net.sourceforge.pmd.lang.rule.properties.wrappers.PropertyDescriptorWrapper;

/**
 * @author Brian Remedios
 */
public class FloatEditorFactory extends AbstractRealNumberEditor<Float> {

    public static final FloatEditorFactory instance = new FloatEditorFactory();


    private FloatEditorFactory() { }


    public PropertyDescriptor<Float> createDescriptor(String name, String description, Control[] otherData) {

        return new FloatProperty(
            name,
            description,
            defaultIn(otherData).floatValue(),
            minimumIn(otherData).floatValue(),
            maximumIn(otherData).floatValue(),
            0.0f
        );
    }


    protected Float valueFrom(Control valueControl) {

        return (float) (((Spinner) valueControl).getSelection() / scale);
    }


    public Control newEditorOn(Composite parent, final PropertyDescriptor<Float> desc, final PropertySource source,
                               final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final FloatProperty fp = floatPropertyFrom(desc);
        final Spinner spinner = newSpinnerFor(parent, source, fp);

        spinner.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                Float newValue = (float) (spinner.getSelection() / scale);
                if (newValue.equals(valueFor(source, fp))) {
                    return;
                }

                source.setProperty(fp, newValue);
                listener.changed(source, fp, newValue);

                adjustRendering(source, desc, spinner);
            }
        });

        return spinner;
    }


    private static FloatProperty floatPropertyFrom(PropertyDescriptor<?> desc) {

        if (desc instanceof PropertyDescriptorWrapper<?>) {
            return (FloatProperty) ((PropertyDescriptorWrapper<?>) desc).getPropertyDescriptor();
        } else {
            return (FloatProperty) desc;
        }
    }
}
