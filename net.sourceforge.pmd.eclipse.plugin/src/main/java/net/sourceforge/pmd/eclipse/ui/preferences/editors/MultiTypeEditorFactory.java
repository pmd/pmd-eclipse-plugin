/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;
import net.sourceforge.pmd.properties.TypeMultiProperty;
import net.sourceforge.pmd.util.ClassUtil;
import net.sourceforge.pmd.util.CollectionUtil;
import net.sourceforge.pmd.util.StringUtil;

/**
 * TODO - use new TypeText widget
 *
 * @author Brian Remedios
 * 
 * @deprecated will be removed
 */
@Deprecated
public final class MultiTypeEditorFactory extends AbstractMultiValueEditorFactory<Class> {

    public static final MultiTypeEditorFactory INSTANCE = new MultiTypeEditorFactory();

    private MultiTypeEditorFactory() { }

    @Override
    public PropertyDescriptor<List<Class>> createDescriptor(String name, String optionalDescription, Control[] otherData) {
        return new TypeMultiProperty(name, "Type value "
            + name, Arrays.asList((Class) String.class), new String[] {"java.lang"}, 0.0f);
    }

    @Override
    protected Control addWidget(Composite parent, Class value, PropertyDescriptor<List<Class>> desc, PropertySource
        source) {
        TypeText typeWidget = new TypeText(parent, SWT.SINGLE | SWT.BORDER, true, "Enter type name");
        setValue(typeWidget, value);
        return typeWidget;
    }

    @Override
    protected void setValue(Control widget, Class value) {
        Class<?> type = (Class<?>) value;
        ((TypeText) widget).setType(type);
    }

    @Override
    protected void configure(final Text textWidget, final PropertyDescriptor<List<Class>> desc,
                             final PropertySource source, final ValueChangeListener listener) {

        textWidget.addListener(SWT.FocusOut, new Listener() {
            @Override
            public void handleEvent(Event event) {
                List<Class> newValue = currentTypes(textWidget);
                List<Class> existingValue = valueFor(source, desc);
                if (existingValue != null && existingValue.equals(newValue)) {
                    return;
                }

                source.setProperty(desc, newValue);
                fillWidget(textWidget, desc, source);   // display the accepted values
                listener.changed(source, desc, newValue);

                adjustRendering(source, desc, textWidget);
            }
        });
    }

    private List<Class> currentTypes(Text textWidget) {
        List<String> typeNames = textWidgetValues(textWidget);
        if (typeNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<Class> types = new ArrayList<>(typeNames.size());

        for (String typeName : typeNames) {
            Class newType = TypeEditorFactory.typeFor(typeName);
            if (newType != null) {
                types.add(newType);
            }
        }
        return types;
    }

    @Override
    protected void fillWidget(Text textWidget, PropertyDescriptor<List<Class>> desc, PropertySource source) {
        List<Class> values = valueFor(source, desc);
        if (values == null) {
            textWidget.setText("");
            return;
        }

        textWidget.setText(asString(values));
        adjustRendering(source, desc, textWidget);
    }


    private String asString(List<Class> types) {
        String[] typeNames = shortNamesFor(types.toArray(new Class<?>[0]));
        return StringUtil.asString(typeNames, DELIMITER + ' ');
    }


    public static String[] shortNamesFor(Class<?>[] types) {
        String[] typeNames = new String[types.length];
        for (int i = 0; i < typeNames.length; i++) {
            typeNames[i] = ClassUtil.asShortestName(types[i]);
        }
        return typeNames;
    }

    @Override
    protected void update(PropertySource source, PropertyDescriptor<List<Class>> desc, List<Class> newValues) {
        source.setProperty(desc, newValues);
    }


    @Override
    protected Class addValueIn(Control widget, PropertyDescriptor<List<Class>> desc, PropertySource source) {

        Class enteredValue = ((TypeText) widget).getType(true);
        if (enteredValue == null) {
            return null;
        }

        List<Class> currentValues = valueFor(source, desc);
        int nAdded = CollectionUtil.addWithoutDuplicates(Collections.singleton(enteredValue), currentValues);
        return nAdded == 0 ? null : enteredValue;
    }

    @Override
    protected List<Class> valueFrom(Control valueControl) {    // not necessary for this type
        return null;
    }
}
