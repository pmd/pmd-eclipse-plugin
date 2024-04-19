/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
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
 * @author Brian Remedios
 * @deprecated This editor factory will be removed without replacement. This was only used for supporting the UI
 *             of the plugin and is considered internal API now.
 */
@Deprecated // for removal
public final class CharacterEditorFactory extends AbstractEditorFactory<Character> {

    public static final CharacterEditorFactory INSTANCE = new CharacterEditorFactory();


    private CharacterEditorFactory() { }


    @Override
    public PropertyDescriptor<Character> createDescriptor(String name, String description, Control[] otherData) {
        return PropertyFactory.charProperty(name).desc(description)
            .defaultValue('a').build();
    }


    @Override
    protected Character valueFrom(Control valueControl) {
        String value = ((Text) valueControl).getText().trim();

        return StringUtils.isBlank(value) || value.length() > 1 ? null
                                                                : value.charAt(0);
    }


    @Override
    public Control newEditorOn(Composite parent, final PropertyDescriptor<Character> desc, final PropertySource
        source, final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);

        fillWidget(text, desc, source);

        text.addListener(SWT.FocusOut, new Listener() {
            @Override
            public void handleEvent(Event event) {
                Character newValue = charValueIn(text);
                Character existingValue = valueFor(source, desc);
                if (existingValue.equals(newValue)) {
                    return;
                }

                source.setProperty(desc, newValue);
                listener.changed(source, desc, newValue);
                adjustRendering(source, desc, text);
            }
        });

        return text;
    }


    protected void fillWidget(Text textWidget, PropertyDescriptor<Character> desc, PropertySource source) {
        Character val = valueFor(source, desc);
        textWidget.setText(val == null ? "" : val.toString());
    }


    private static Character charValueIn(Text textControl) {
        String newValue = textControl.getText().trim();
        if (newValue.length() == 0) {
            return null;
        }
        return newValue.charAt(0);
    }
}
