/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.util.regex.Pattern;

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
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * @deprecated This editor factory will be removed without replacement. This was only used for supporting the UI
 *             of the plugin and is considered internal API now.
 */
@Deprecated // for removal
public class RegexEditorFactory extends AbstractEditorFactory<Pattern> {

    public static final RegexEditorFactory INSTANCE = new RegexEditorFactory();

    protected RegexEditorFactory() {
        // protected constructor for subclassing
    }

    @Override
    public PropertyDescriptor<Pattern> createDescriptor(String name, String description, Control[] otherData) {
        return PropertyFactory.regexProperty(name).desc(description)
            .defaultValue(valueFrom(otherData[1]))
            .build();
    }

    @Override
    protected Pattern valueFrom(Control valueControl) {
        return Pattern.compile(((Text) valueControl).getText());
    }

    @Override
    public Control newEditorOn(Composite parent, final PropertyDescriptor<Pattern> desc, final PropertySource source,
                               final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fillWidget(text, desc, source);

        text.addListener(SWT.FocusOut, new Listener() {
            @Override
            public void handleEvent(Event event) {
                String newValue = text.getText().trim();
                String existingValue = valueFor(source, desc).pattern();
                if (StringUtils.equals(StringUtils.trimToNull(existingValue), StringUtils.trimToNull(newValue))) {
                    return;
                }

                setValue(source, desc, Pattern.compile(newValue));
                fillWidget(text, desc, source);     // redraw
                listener.changed(source, desc, newValue);
            }
        });

        return text;
    }


    protected void fillWidget(Text textWidget, PropertyDescriptor<Pattern> desc, PropertySource source) {
        String val = valueFor(source, desc).pattern();
        textWidget.setText(val);
        adjustRendering(source, desc, textWidget);
    }


    private void setValue(PropertySource source, PropertyDescriptor<Pattern> desc, Pattern value) {
        if (!source.hasDescriptor(desc)) {
            return;
        }
        source.setProperty(desc, value);
    }
}
