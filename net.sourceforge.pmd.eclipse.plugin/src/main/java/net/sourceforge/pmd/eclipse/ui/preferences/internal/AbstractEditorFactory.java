/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import net.sourceforge.pmd.eclipse.ui.preferences.br.EditorFactory;
import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * @author Brian Remedios
 */
public abstract class AbstractEditorFactory<T> implements EditorFactory<T> {


    protected AbstractEditorFactory() {
        // protected default constructor for subclassing
    }

    /**
     * Generic control that provides a label/widget pair for the default value.
     * Subclasses can override this to provide additional labels & widgets as
     * necessary but must be able to extract the values held by them when the
     * property is created.
     */
    @Override
    public Control[] createOtherControlsOn(Composite parent, PropertyDescriptor<T> desc, PropertySource source,
            ValueChangeListener listener, SizeChangeListener sizeListener) {
        return new Control[] { newLabel(parent, "Default"), newEditorOn(parent, desc, source, listener, sizeListener) };
    }

    protected Label newLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.None);
        label.setText(text);
        return label;
    }

    protected abstract T valueFrom(Control valueControl);

    protected T valueFor(PropertySource source, PropertyDescriptor<T> desc) {

        return source.hasDescriptor(desc) ? source.getProperty(desc) : desc.defaultValue();
    }

    @Override
    public Label addLabel(Composite parent, PropertyDescriptor<T> desc) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(desc.name() + " (" + desc.description() + ")");
        GridData data = new GridData();
        data.horizontalAlignment = SWT.LEFT;
        // CENTER is preferred only when showing a single row value widget...hmm
        data.verticalAlignment = SWT.CENTER; 
        label.setLayoutData(data);
        return label;
    }
}
