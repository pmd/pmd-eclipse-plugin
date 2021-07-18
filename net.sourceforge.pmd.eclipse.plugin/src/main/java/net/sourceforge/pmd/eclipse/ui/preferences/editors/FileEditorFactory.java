/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.io.File;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.properties.FileProperty;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * @author Brian Remedios
 *
 * @deprecated will be removed
 */
@Deprecated
public class FileEditorFactory extends AbstractEditorFactory<File> {


    public static final FileEditorFactory INSTANCE = new FileEditorFactory();


    protected FileEditorFactory() {
        // protected default constructor for subclassing
    }


    @Override
    public PropertyDescriptor<File> createDescriptor(String name, String description, Control[] otherData) {
        return new FileProperty(
            name,
            description,
            new File(""),
            0.0f
        );
    }


    @Override
    public Control newEditorOn(Composite parent, final PropertyDescriptor<File> desc, final PropertySource source,
                               final ValueChangeListener listener, SizeChangeListener sizeListener) {

        final FilePicker picker = new FilePicker(parent, SWT.SINGLE | SWT.BORDER, "Open", null);
        picker.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fillWidget(picker, desc, source);

        picker.addFocusOutListener(new Listener() {
            @Override
            public void handleEvent(Event event) {
                File newValue = picker.getFile();
                File existingValue = valueFor(source, desc);
                if (areSemanticEquals(existingValue, newValue)) {
                    return;
                }

                setValue(source, desc, newValue);
                fillWidget(picker, desc, source);     // redraw
                listener.changed(source, desc, newValue);
            }
        });

        return picker;
    }


    protected void fillWidget(FilePicker fileWidget, PropertyDescriptor<File> desc, PropertySource source) {
        File val = valueFor(source, desc);
        fileWidget.setFile(val);
        adjustRendering(source, desc, fileWidget);
    }


    public static boolean areSemanticEquals(File fileA, File fileB) {
        return Objects.equals(fileA, fileB);
    }


    private void setValue(PropertySource source, PropertyDescriptor<File> desc, File value) {

        if (!source.hasDescriptor(desc)) {
            return;
        }
        source.setProperty(desc, value);
    }


    @Override
    protected File valueFrom(Control valueControl) {
        return ((FilePicker) valueControl).getFile();
    }

}
