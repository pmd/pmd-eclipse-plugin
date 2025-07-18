/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui;

import java.util.Comparator;

import org.eclipse.swt.graphics.Image;

public class ItemFieldAccessorAdapter<T extends Object, V extends Object> implements ItemFieldAccessor<T, V> {

    private final Comparator<T> comparator; // can be null

    public ItemFieldAccessorAdapter(Comparator<T> aComparator) {
        comparator = aComparator;
    }

    @Override
    public Comparator<T> comparator() {
        return comparator;
    }

    @Override
    public T valueFor(V item) {
        return null;
    }

    @Override
    public Image imageFor(V item) {
        return null;
    }

    @Override
    public String labelFor(V item) {
        return null;
    }
}
