/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.ast;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * 
 * @author Brian Remedios
 */
public class ASTLabelProvider implements ILabelProvider {

    @Override
    public void addListener(ILabelProviderListener listener) {
        // TODO
    }

    @Override
    public void dispose() {
        // TODO
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        // TODO
    }

    @Override
    public Image getImage(Object element) {
        return null;
    }

    @Override
    public String getText(Object element) {
        // TODO
        return null;
        // AbstractNode node = (AbstractNode)element;
        // String extra = node.getImage();
        //
        // return extra == null ?
        // node.toString() :
        // node.toString() + ": " + extra;
    }

}
