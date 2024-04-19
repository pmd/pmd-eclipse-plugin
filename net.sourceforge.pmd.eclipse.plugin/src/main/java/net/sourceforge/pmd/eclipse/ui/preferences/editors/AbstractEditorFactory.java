/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import org.eclipse.swt.widgets.Control;

import net.sourceforge.pmd.eclipse.util.ColourManager;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * @author Brian Remedios
 * @deprecated This editor factory will be removed without replacement. This was only used for supporting the UI
 *             of the plugin and is considered internal API now.
 */
@Deprecated // for removal
public abstract class AbstractEditorFactory<T> extends net.sourceforge.pmd.eclipse.ui.preferences.internal.AbstractEditorFactory<T> {
    /**
     * @deprecated not used
     */
    @Deprecated // for removal
    protected static ColourManager colourManager;


    /**
     * Return the value as a string that can be easily recognized and parsed
     * when we see it again.
     *
     * @param value
     *            Object
     *
     * @return String
     * @deprecated not used
     */
    @Deprecated // for removal
    protected static String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    /**
     * @deprecated not overridden by any subtype, so not used
     */
    @Deprecated // for removal
    protected void adjustRendering(PropertySource source, PropertyDescriptor<?> desc, Control control) {
        // intended to be overridden
    }
}
