/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import net.sourceforge.pmd.properties.PropertyDescriptor;

/**
 *
 * @author Brian Remedios
 */
public interface PropertyChangeListener {

    void changed(PropertyDescriptor<?> descriptor, Object newValue);
}
