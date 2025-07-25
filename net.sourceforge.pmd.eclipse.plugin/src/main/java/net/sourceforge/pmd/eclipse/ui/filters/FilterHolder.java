/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.filters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Brian Remedios
 */
class FilterHolder {

    public String pattern;
    public boolean forPMD;
    public boolean forCPD;
    public boolean isInclude;

    public static final FilterHolder[] EMPTY_HOLDERS = new FilterHolder[0];

    public static final Accessor EXCLUDE_ACCESSOR = new BasicAccessor() {
        @Override
        public boolean boolValueFor(FilterHolder fh) {
            return !fh.isInclude;
        }
    };

    public static final Accessor INCLUDE_ACCESSOR = new BasicAccessor() {
        @Override
        public boolean boolValueFor(FilterHolder fh) {
            return fh.isInclude;
        }
    };

    public static final Accessor PMD_ACCESSOR = new BasicAccessor() {
        @Override
        public boolean boolValueFor(FilterHolder fh) {
            return fh.forPMD;
        }
    };

    public static final Accessor CPD_ACCESSOR = new BasicAccessor() {
        @Override
        public boolean boolValueFor(FilterHolder fh) {
            return fh.forCPD;
        }
    };

    public static final Accessor PATTERN_ACCESSOR = new BasicAccessor() {
        @Override
        public String textValueFor(FilterHolder fh) {
            return fh.pattern;
        }
    };

    FilterHolder(String thePattern, boolean pmdFlag, boolean cpdFlag, boolean isIncludeFlag) {
        pattern = thePattern;
        forPMD = pmdFlag;
        forCPD = cpdFlag;
        isInclude = isIncludeFlag;
    }

    public interface Accessor {
        boolean boolValueFor(FilterHolder fh);

        String textValueFor(FilterHolder fh);
    }

    public static Boolean boolValueOf(Collection<FilterHolder> holders, Accessor boolAccessor) {
        Set<Boolean> values = new HashSet<>();
        for (FilterHolder fh : holders) {
            values.add(boolAccessor.boolValueFor(fh));
        }
        int valueCount = values.size();
        return valueCount == 2 || valueCount == 0 ? null : values.iterator().next();
    }

    public static String textValueOf(Collection<FilterHolder> holders, Accessor textAccessor) {
        Set<String> values = new HashSet<>();
        for (FilterHolder fh : holders) {
            values.add(textAccessor.textValueFor(fh));
        }
        return values.size() == 1 ? values.iterator().next() : "";
    }
}
