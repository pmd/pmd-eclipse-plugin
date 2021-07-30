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
import net.sourceforge.pmd.properties.FloatProperty;
import net.sourceforge.pmd.properties.NumericPropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * @author Brian Remedios
 *
 * @deprecated This type is unused and won't be supported in the future by PMD
 */
@Deprecated
public final class FloatEditorFactory extends AbstractRealNumberEditor<Float> {

    public static final FloatEditorFactory INSTANCE = new FloatEditorFactory();


    private FloatEditorFactory() { }


    @Override
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


    @Override
    protected Float valueFrom(Control valueControl) {
        return (float) (((Spinner) valueControl).getSelection() / SCALE);
    }


    @Override
    public Control newEditorOn(Composite parent, final PropertyDescriptor<Float> desc, final PropertySource source,
                               final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final Spinner spinner = newSpinnerFor(parent, source, (NumericPropertyDescriptor<Float>) desc);

        spinner.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                Float newValue = (float) (spinner.getSelection() / SCALE);
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
}
