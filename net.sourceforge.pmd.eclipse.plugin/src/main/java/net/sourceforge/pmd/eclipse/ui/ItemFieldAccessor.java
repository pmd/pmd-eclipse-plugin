/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui;

import java.util.Comparator;

import org.eclipse.swt.graphics.Image;

/**
 * 
 * @author Brian Remedios
 */
public interface ItemFieldAccessor<T extends Object, V extends Object> {

    T valueFor(V item);

    Image imageFor(V item);

    String labelFor(V item);

    Comparator<T> comparator();
}
