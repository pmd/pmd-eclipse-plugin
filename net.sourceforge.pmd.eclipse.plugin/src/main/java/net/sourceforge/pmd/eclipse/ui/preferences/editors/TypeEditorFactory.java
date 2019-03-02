/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;
import net.sourceforge.pmd.properties.TypeProperty;
import net.sourceforge.pmd.util.ClassUtil;


/**
 * @author Brian Remedios
 */
public class TypeEditorFactory extends AbstractEditorFactory<Class> {

    public static final TypeEditorFactory INSTANCE = new TypeEditorFactory();


    private TypeEditorFactory() { }


    public PropertyDescriptor<Class> createDescriptor(String name, String description, Control[] otherData) {

        return new TypeProperty(
            name,
            description,
            String.class,
            new String[] {"java.lang"},
            0.0f
        );
    }


    protected Class valueFrom(Control valueControl) {
        return ((TypeText) valueControl).getType(false);
    }


    public Control newEditorOn(Composite parent, final PropertyDescriptor<Class> desc, final PropertySource source,
                               final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final TypeText typeText = new TypeText(parent,
                                               SWT.SINGLE | SWT.BORDER, true, "Enter a type name");  // TODO  i18l
        typeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fillWidget(typeText, desc, source);

        Listener wereDoneListener = new Listener() {
            public void handleEvent(Event event) {
                Class<?> newValue = typeText.getType(true);
                if (newValue == null) {
                    return;
                }

                Class<?> existingValue = (Class<?>) valueFor(source, desc);
                if (existingValue == newValue) {
                    return;
                }

                source.setProperty(desc, newValue);
                listener.changed(source, desc, newValue);

                adjustRendering(source, desc, typeText);
            }
        };

        typeText.addListener(SWT.FocusOut, wereDoneListener);
        typeText.addListener(SWT.DefaultSelection, wereDoneListener);
        return typeText;
    }


    protected void fillWidget(TypeText textWidget, PropertyDescriptor<Class> desc, PropertySource source) {

        Class<?> type = (Class<?>) valueFor(source, desc);
        textWidget.setType(type);
    }


    public static Class<?> typeFor(String typeName) {

        Class<?> newType = ClassUtil.getTypeFor(typeName);    // try for well-known types first
        if (newType != null) {
            return newType;
        }

        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
