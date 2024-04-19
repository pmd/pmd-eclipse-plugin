/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

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
public final class BooleanEditorFactory extends AbstractEditorFactory<Boolean> {

    public static final BooleanEditorFactory INSTANCE = new BooleanEditorFactory();


    private BooleanEditorFactory() { }


    @Override
    public PropertyDescriptor<Boolean> createDescriptor(String name, String description, Control[] otherData) {
        return PropertyFactory.booleanProperty(name).desc(description)
            .defaultValue(otherData != null && valueFrom(otherData[1]))
            .build();
    }


    @Override
    protected Boolean valueFrom(Control valueControl) {
        return ((Button) valueControl).getSelection();
    }


    @Override
    public Control newEditorOn(Composite parent, final PropertyDescriptor<Boolean> desc, final PropertySource source,
                               final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final Button butt = new Button(parent, SWT.CHECK);
        butt.setText("");

        boolean set = valueFor(source, desc);
        butt.setSelection(set);

        SelectionAdapter sa = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean selected = butt.getSelection();
                if (selected == valueFor(source, desc)) {
                    return;
                }

                source.setProperty(desc, selected);
                listener.changed(source, desc, selected);
                adjustRendering(source, desc, butt);
            }
        };

        butt.addSelectionListener(sa);

        return butt;
    }
}
