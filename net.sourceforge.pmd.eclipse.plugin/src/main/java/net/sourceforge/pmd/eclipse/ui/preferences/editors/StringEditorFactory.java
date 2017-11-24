package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;
import net.sourceforge.pmd.properties.StringProperty;
import net.sourceforge.pmd.util.StringUtil;

/**
 * @author Brian Remedios
 */
public class StringEditorFactory extends AbstractEditorFactory<String> {

    public static final StringEditorFactory instance = new StringEditorFactory();


    protected StringEditorFactory() { }


    public PropertyDescriptor<String> createDescriptor(String name, String description, Control[] otherData) {

        return new StringProperty(
            name,
            description,
            otherData == null ? "" : valueFrom(otherData[1]),
            0.0f
        );
    }


    protected String valueFrom(Control valueControl) {
        return ((Text) valueControl).getText();
    }


    public Control newEditorOn(Composite parent, final PropertyDescriptor<String> desc, final PropertySource source,
                               final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fillWidget(text, desc, source);

        text.addListener(SWT.FocusOut, new Listener() {
            public void handleEvent(Event event) {
                String newValue = text.getText().trim();
                String existingValue = valueFor(source, desc);
                if (StringUtil.areSemanticEquals(existingValue, newValue)) {
                    return;
                }

                setValue(source, desc, newValue);
                fillWidget(text, desc, source);     // redraw
                listener.changed(source, desc, newValue);
            }
        });

        return text;
    }


    protected void fillWidget(Text textWidget, PropertyDescriptor<String> desc, PropertySource source) {
        String val = valueFor(source, desc);
        textWidget.setText(val == null ? "" : val);
        adjustRendering(source, desc, textWidget);
    }


    private void setValue(PropertySource source, PropertyDescriptor<String> desc, String value) {

        if (!source.hasDescriptor(desc)) {
            return;
        }
        source.setProperty(desc, value);
    }
}
