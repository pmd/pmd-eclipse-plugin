package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.PropertySource;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.eclipse.util.Util;
import net.sourceforge.pmd.lang.rule.properties.MethodMultiProperty;
import net.sourceforge.pmd.util.ClassUtil;
import net.sourceforge.pmd.util.CollectionUtil;

/**
 * @author Brian Remedios
 */
public class MultiMethodEditorFactory extends AbstractMultiValueEditorFactory<Method> {

    public static final MultiMethodEditorFactory instance = new MultiMethodEditorFactory();


    private MultiMethodEditorFactory() { }


    public PropertyDescriptor<List<Method>> createDescriptor(String name, String optionalDescription, Control[] otherData) {
        return new MethodMultiProperty(name, "Method value "
            + name, new Method[] {MethodEditorFactory.stringLength}, new String[] {"java.lang"}, 0.0f);
    }


    protected void fillWidget(Text textWidget, PropertyDescriptor<List<Method>> desc, PropertySource source) {

        List<Method> values = valueFor(source, desc);
        if (values == null) {
            textWidget.setText("");
            return;
        }

        Map<String, List<Method>> methodMap = ClassUtil.asMethodGroupsByTypeName(values);
        textWidget.setText(asString(methodMap));

        adjustRendering(source, desc, textWidget);
    }


    private static String asString(Map<String, List<Method>> methodGroups) {

        if (methodGroups.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, List<Method>>> iter = methodGroups.entrySet().iterator();
        Entry<String, List<Method>> entry = iter.next();

        sb.append(entry.getKey()).append('[');
        allSignaturesOn(sb, entry.getValue(), ",");
        sb.append(']');

        while (iter.hasNext()) {
            entry = iter.next();
            sb.append("  ").append(entry.getKey()).append('[');
            allSignaturesOn(sb, entry.getValue(), ", ");
            sb.append(']');
        }

        return sb.toString();
    }


    private static void allSignaturesOn(StringBuilder sb, List<Method> methods, String delimiter) {

        sb.append(
            Util.signatureFor(methods.get(0), MethodEditorFactory.UnwantedPrefixes)
        );

        for (int i = 1; i < methods.size(); i++) {
            sb.append(delimiter).append(
                Util.signatureFor(methods.get(i), MethodEditorFactory.UnwantedPrefixes)
            );
        }
    }


    @Override
    protected Control addWidget(Composite parent, Method value, PropertyDescriptor<List<Method>> desc,
                                PropertySource source) {
        MethodPicker widget = new MethodPicker(parent, SWT.SINGLE | SWT.BORDER, MethodEditorFactory.UnwantedPrefixes);
        setValue(widget, value);
        return widget;
    }


    @Override
    protected void setValue(Control widget, Method value) {
        ((MethodPicker) widget).setMethod(value);
    }


    @Override
    protected void configure(final Text textWidget, final PropertyDescriptor<List<Method>> desc,
                             final PropertySource source, final ValueChangeListener listener) {
        textWidget.setEditable(false);
    }


    @Override
    protected void update(PropertySource source, PropertyDescriptor<List<Method>> desc, List<Method> newValues) {
        source.setProperty(desc, newValues);
    }


    @Override
    protected Method addValueIn(Control widget, PropertyDescriptor<List<Method>> desc, PropertySource source) {

        Method newValue = ((MethodPicker) widget).getMethod();
        if (newValue == null) {
            return null;
        }

        List<Method> currentValues = valueFor(source, desc);
        int nAdded = CollectionUtil.addWithoutDuplicates(Collections.singleton(newValue), currentValues);
        return nAdded == 0 ? null : newValue;
    }


    protected List<Method> valueFrom(Control valueControl) {    // not necessary for this type
        return null;
    }


    public static String[] signaturesFor(Method[] methods) {
        String[] typeNames = new String[methods.length];
        for (int i = 0; i < typeNames.length; i++) {
            typeNames[i] = Util.signatureFor(methods[i], MethodEditorFactory.UnwantedPrefixes);
        }
        return typeNames;
    }
}
