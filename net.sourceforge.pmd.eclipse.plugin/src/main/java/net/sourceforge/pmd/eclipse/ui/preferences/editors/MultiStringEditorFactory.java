package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.PropertySource;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.lang.rule.properties.StringMultiProperty;
import net.sourceforge.pmd.lang.rule.properties.wrappers.PropertyDescriptorWrapper;
import net.sourceforge.pmd.util.CollectionUtil;
import net.sourceforge.pmd.util.StringUtil;

/**
 * @author Brian Remedios
 */
public class MultiStringEditorFactory extends AbstractMultiValueEditorFactory<String> {

    public static final MultiStringEditorFactory instance = new MultiStringEditorFactory();


    private MultiStringEditorFactory() { }


    public PropertyDescriptor<List<String>> createDescriptor(String name, String optionalDescription,
                                                             Control[] otherData) {

        return new StringMultiProperty(name, "String value "
            + name, new String[] {""}, 0.0f, StringMultiProperty.DEFAULT_DELIMITER);
    }


    protected Control addWidget(Composite parent, Object value, PropertyDescriptor<List<String>> desc,
                                PropertySource source) {
        Text textWidget = new Text(parent, SWT.SINGLE | SWT.BORDER);
        setValue(textWidget, value);
        return textWidget;
    }


    protected void setValue(Control widget, Object value) {
        ((Text) widget).setText(value == null ? "" : value.toString());
    }


    protected void configure(final Text textWidget, final PropertyDescriptor<List<String>> desc,
                             final PropertySource source, final ValueChangeListener listener) {

        final StringMultiProperty smp = multiStringPropertyFrom(desc);

        Listener widgetListener = new Listener() {
            public void handleEvent(Event event) {
                List<String> newValues = textWidgetValues(textWidget);
                List<String> existingValues = valueFor(source, smp);
                if (CollectionUtil.areSemanticEquals(existingValues, newValues)) {
                    return;
                }

                source.setProperty(smp, newValues);
                fillWidget(textWidget, desc, source);    // reload with latest scrubbed values
                listener.changed(source, desc, newValues);
            }
        };

        textWidget.addListener(SWT.FocusOut, widgetListener);
        // textWidget.addListener(SWT.DefaultSelection, widgetListener);
    }


    private static StringMultiProperty multiStringPropertyFrom(PropertyDescriptor<List<String>> desc) {

        if (desc instanceof PropertyDescriptorWrapper<?>) {
            return (StringMultiProperty) ((PropertyDescriptorWrapper<?>) desc).getPropertyDescriptor();
        } else {
            return (StringMultiProperty) desc;
        }
    }


    @Override
    protected void update(PropertySource source, PropertyDescriptor<List<String>> desc, List<String> newValues) {
        source.setProperty(desc, newValues);
    }


    @Override
    protected String addValueIn(Control widget, PropertyDescriptor<List<String>> desc, PropertySource source) {

        String newValue = ((Text) widget).getText().trim();
        if (StringUtil.isEmpty(newValue)) {
            return null;
        }

        List<String> currentValues = valueFor(source, desc);
        int nAdded = CollectionUtil.addWithoutDuplicates(Collections.singleton(newValue), currentValues);
        return (nAdded == 0) ? null : newValue;
    }


    protected Object valueFrom(Control valueControl) {    // not necessary for this type
        return null;
    }
}
