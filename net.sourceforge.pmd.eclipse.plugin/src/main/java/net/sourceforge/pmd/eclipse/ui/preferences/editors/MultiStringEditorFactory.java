/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.properties.PropertySource;
import net.sourceforge.pmd.util.CollectionUtil;

/**
 * @author Brian Remedios
 */
public final class MultiStringEditorFactory extends AbstractMultiValueEditorFactory<String> {

    public static final MultiStringEditorFactory INSTANCE = new MultiStringEditorFactory();


    private MultiStringEditorFactory() { }

    @Override
    public PropertyDescriptor<List<String>> createDescriptor(String name, String optionalDescription,
                                                             Control[] otherData) {
        return PropertyFactory.stringListProperty(name).desc(optionalDescription).emptyDefaultValue().build();
    }


    @Override
    protected Control addWidget(Composite parent, String value, PropertyDescriptor<List<String>> desc,
                                PropertySource source) {
        Text textWidget = new Text(parent, SWT.SINGLE | SWT.BORDER);
        setValue(textWidget, value);
        return textWidget;
    }


    @Override
    protected void setValue(Control widget, String value) {
        ((Text) widget).setText(value == null ? "" : value);
    }

    @Override
    protected void configure(final Text textWidget, final PropertyDescriptor<List<String>> desc,
                             final PropertySource source, final ValueChangeListener listener) {


        Listener widgetListener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                List<String> newValues = textWidgetValues(textWidget);
                List<String> existingValues = valueFor(source, desc);
                if (existingValues != null && existingValues.equals(newValues)) {
                    return;
                }

                source.setProperty(desc, newValues);
                fillWidget(textWidget, desc, source);    // reload with latest scrubbed values
                listener.changed(source, desc, newValues);
            }
        };

        textWidget.addListener(SWT.FocusOut, widgetListener);
        // textWidget.addListener(SWT.DefaultSelection, widgetListener);
    }


    @Override
    protected void update(PropertySource source, PropertyDescriptor<List<String>> desc, List<String> newValues) {
        source.setProperty(desc, newValues);
    }


    @Override
    protected String addValueIn(Control widget, PropertyDescriptor<List<String>> desc, PropertySource source) {

        String newValue = ((Text) widget).getText().trim();
        if (StringUtils.isBlank(newValue)) {
            return null;
        }

        List<String> currentValues = valueFor(source, desc);
        int nAdded = CollectionUtil.addWithoutDuplicates(Collections.singleton(newValue), currentValues);
        return (nAdded == 0) ? null : newValue;
    }

    @Override
    protected List<String> valueFrom(Control valueControl) {    // not necessary for this type
        return Collections.emptyList();
    }
}
