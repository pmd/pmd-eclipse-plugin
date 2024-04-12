/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.internal;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.editors.AbstractEditorFactory;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.properties.PropertySource;

public class PropertyEditorFactory extends AbstractEditorFactory {

    public static final PropertyEditorFactory INSTANCE = new PropertyEditorFactory();


    private PropertyEditorFactory() {
    }

    @Override
    public PropertyDescriptor<?> createDescriptor(String name, String description, Control[] otherData) {
        return PropertyFactory.stringProperty(name).desc(description)
            .defaultValue(otherData == null ? "" : valueFrom(otherData[1]))
            .build();
    }


    @Override
    protected String valueFrom(Control valueControl) {
        return ((Text) valueControl).getText();
    }


    @Override
    public Control newEditorOn(Composite parent, final PropertyDescriptor desc, final PropertySource source,
                               final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fillWidget(text, desc, source);

        text.addListener(SWT.FocusOut, new Listener() {
            @Override
            public void handleEvent(Event event) {
                String newValue = text.getText().trim();
                Object existingValueReal = valueFor(source, desc);
                String existingValue = desc.serializer().toString(existingValueReal);

                if (StringUtils.equals(StringUtils.trimToNull(existingValue), StringUtils.trimToNull(newValue))) {
                    return;
                }

                setValue(source, desc, newValue);
                fillWidget(text, desc, source);     // redraw
                listener.changed(source, desc, newValue);
            }
        });

        return text;
    }


    protected void fillWidget(Text textWidget, PropertyDescriptor desc, PropertySource source) {
        Object realVal = valueFor(source, desc);
        String val = desc.serializer().toString(realVal);
        textWidget.setText(val == null ? "" : val);
        adjustRendering(source, desc, textWidget);
    }


    private void setValue(PropertySource source, PropertyDescriptor desc, String value) {
        if (!source.hasDescriptor(desc)) {
            return;
        }
        Object realValue = desc.serializer().fromString(value);
        source.setProperty(desc, realValue);
    }
}
