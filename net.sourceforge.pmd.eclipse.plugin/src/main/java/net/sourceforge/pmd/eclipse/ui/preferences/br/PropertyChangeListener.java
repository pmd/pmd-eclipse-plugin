
package net.sourceforge.pmd.eclipse.ui.preferences.br;

import net.sourceforge.pmd.properties.PropertyDescriptor;

/**
 *
 * @author Brian Remedios
 */
public interface PropertyChangeListener {

    void changed(PropertyDescriptor<?> descriptor, Object newValue);
}
