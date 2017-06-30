package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.PropertySource;
import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.lang.rule.properties.MethodProperty;
import net.sourceforge.pmd.util.ClassUtil;

/**
 * @author Brian Remedios
 */
public class MethodEditorFactory extends AbstractEditorFactory<Method> {

    public static final MethodEditorFactory instance = new MethodEditorFactory();
    public static final String[] UnwantedPrefixes = new String[] {
        "java.lang.reflect.",
        "java.lang.",
        "java.util."
    };

    public static final Method stringLength = ClassUtil.methodFor(String.class, "length", ClassUtil.EMPTY_CLASS_ARRAY);


    private MethodEditorFactory() { }


    public PropertyDescriptor<Method> createDescriptor(String name, String optionalDescription, Control[] otherData) {
        return new MethodProperty(name, "Method value " + name, stringLength, new String[] {"java.lang"}, 0.0f);
    }


    protected Method valueFrom(Control valueControl) {

        return ((MethodPicker) valueControl).getMethod();
    }


    public Control newEditorOn(Composite parent, final PropertyDescriptor<Method> desc, final PropertySource source,
                               final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final MethodPicker picker = new MethodPicker(parent, SWT.SINGLE | SWT.BORDER, UnwantedPrefixes);
        picker.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fillWidget(picker, desc, source);

        picker.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Method newValue = picker.getMethod();
                if (newValue == null) {
                    return;
                }

                Method existingValue = valueFor(source, desc);
                if (existingValue == newValue) {
                    return;
                }

                source.setProperty(desc, newValue);
                fillWidget(picker, desc, source);     // redraw
                listener.changed(source, desc, newValue);
            }
        });

        return picker;
    }


    protected void fillWidget(MethodPicker widget, PropertyDescriptor<Method> desc, PropertySource source) {

        Method method = valueFor(source, desc);
        widget.setMethod(method);
        adjustRendering(source, desc, widget);
    }
}
