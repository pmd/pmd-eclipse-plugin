/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A string that maintains a set of interesting indicies about itself.
 * 
 * @author Brian Remedios
 */
public class IndexedString implements Comparable<IndexedString> {

    public final String string;
    public final List<int[]> indexSpans;

    public static final IndexedString EMPTY = new IndexedString("");

    public IndexedString(String theString) {
        this(theString, Collections.<int[]>emptyList());
    }

    public IndexedString(String theString, List<int[]> theSpans) {
        string = theString;
        indexSpans = theSpans;
    }

    @Override
    public int compareTo(IndexedString other) {
        int deltaLength = other.string.length() - string.length();

        return deltaLength == 0 ? other.string.compareTo(string) : deltaLength;
    }

    @Override
    public int hashCode() {
        return Objects.hash(string);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IndexedString other = (IndexedString) obj;
        return compareTo(other) == 0;
    }
}
