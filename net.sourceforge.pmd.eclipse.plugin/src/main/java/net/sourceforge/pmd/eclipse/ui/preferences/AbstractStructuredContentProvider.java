/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Base class for IStructuredContentProviders.
 * 
 * @see IStructuredContentProvider
 *
 */
public abstract class AbstractStructuredContentProvider implements IStructuredContentProvider {

    @Override
    public void dispose() {
        // to be overridden
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // to be overriden
    }
}
