package net.sourceforge.pmd.eclipse.ui.preferences.br;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.PropertySource;

/**
 * @author Brian Remedios
 */
public interface EditorFactory<T> {

    /**
     * Have the factory create a descriptor using the name and description provided and use any
     * of the values in the otherData widgets provided by the createOtherControlsOn method called
     * earlier.
     *
     * @param name
     * @param otherData
     *
     * @return PropertyDescriptor<T>
     */
    PropertyDescriptor<T> createDescriptor(String name, String description, Control[] otherData);


    /**
     * Instantiate and return a label for the descriptor on the parent provided.
     *
     * @param parent
     * @param desc
     *
     * @return
     */
    Label addLabel(Composite parent, PropertyDescriptor<T> desc);


    /**
     * Creates a property value editor widget(s) on the parent for the specified descriptor
     * and rule. It does not perform any layout operations or set form attachments.
     *
     * @param parent   Composite
     * @param desc     PropertyDescriptor
     * @param rule     Rule
     * @param listener ValueChangeListener
     *
     * @return Control
     */
    Control newEditorOn(Composite parent, PropertyDescriptor<T> desc, PropertySource source, ValueChangeListener
        listener, SizeChangeListener sizeListener);


    /**
     * Create an array of label-widget pairs on the parent composite for the
     * type managed by the factory. In most cases this will just be a single
     * widget that captures the default value. Numeric types may also provide
     * min/max limit widgets.
     *
     * @param parent
     * @param desc
     * @param rule
     * @param listener
     * @param sizeListener
     *
     * @return Control[]
     */
    Control[] createOtherControlsOn(Composite parent, PropertyDescriptor<T> desc, PropertySource source,
                                    ValueChangeListener listener, SizeChangeListener sizeListener);
}
