/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import net.sourceforge.pmd.eclipse.ui.preferences.br.EditorFactory;
import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.eclipse.util.ColourManager;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * @author Brian Remedios
 */
public abstract class AbstractEditorFactory<T> implements EditorFactory<T> {

    protected static ColourManager colourManager;
    // protected static Color overriddenColour;

    protected AbstractEditorFactory() {
    }

    // private static ColourManager colourManager() {
    //
    // if (colourManager != null) return colourManager;
    // colourManager = ColourManager.managerFor(Display.getCurrent());
    // return colourManager;
    // }

    // private Color overriddenColour() {
    //
    // if (overriddenColour != null) return overriddenColour;
    //
    // overriddenColour =
    // colourManager().colourFor(AbstractRulePanelManager.overridenColourValues);
    // return overriddenColour;
    // }

    /**
     * Generic control that provides a label/widget pair for the default value.
     * Subclasses can override this to provide additional labels & widgets as
     * necessary but must be able to extract the values held by them when the
     * property is created.
     */
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

    /**
     * Method addLabel.
     *
     * @param parent
     *            Composite
     * @param desc
     *            PropertyDescriptor
     *
     * @return Label
     */
    public Label addLabel(Composite parent, PropertyDescriptor<T> desc) {

        Label label = new Label(parent, SWT.NONE);
        label.setText(desc.description());
        GridData data = new GridData();
        data.horizontalAlignment = SWT.LEFT;
        // CENTER is preferred only when showing a single row value widget...hmm
        data.verticalAlignment = SWT.CENTER; 
        label.setLayoutData(data);
        return label;
    }

    /**
     * Adjust the display of the control to denote whether it holds onto the
     * default value or not.
     *
     * @param control
     * @param hasDefaultValue
     */
    // protected void adjustRendering(Control control, boolean hasDefaultValue)
    // {
    //
    // Display display = control.getDisplay();
    //
    // control.setBackground(
    // display.getSystemColor(hasDefaultValue ? SWT.COLOR_WHITE :
    // SWT.COLOR_CYAN)
    // );
    // }
    protected void adjustRendering(PropertySource source, PropertyDescriptor<?> desc, Control control) {

        return; // don't do it...kinda irritating

        // if (!(rule instanceof RuleReference)) return;
        //
        // boolean isOverridden =
        // ((RuleReference)rule).hasOverriddenProperty(desc);
        // Display display = control.getDisplay();
        // Color clr = isOverridden ? overriddenColour() :
        // display.getSystemColor(SWT.COLOR_WHITE);
        //
        // control.setBackground(clr);
    }

    /**
     * Return the value as a string that can be easily recognized and parsed
     * when we see it again.
     *
     * @param value
     *            Object
     *
     * @return String
     */
    protected static String asString(Object value) {
        return value == null ? "" : value.toString();
    }

}
