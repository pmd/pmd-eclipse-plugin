/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.filters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import net.sourceforge.pmd.eclipse.ui.ItemColumnDescriptor;
import net.sourceforge.pmd.eclipse.ui.ItemFieldAccessor;
import net.sourceforge.pmd.eclipse.ui.ItemFieldAccessorAdapter;
import net.sourceforge.pmd.eclipse.util.Util;

/**
 *
 * @author Brian Remedios
 */
public final class FilterColumnUI {
    private FilterColumnUI() {
        // utility / constants class
    }

    public static final ItemFieldAccessor<String, FilterHolder> INCLUDE_ACCESSOR = new ItemFieldAccessorAdapter<String, FilterHolder>(
            null) {
        @Override
        public Image imageFor(FilterHolder holder) {
            return FilterPreferencesPage.typeIconFor(holder);
        }
    };

    public static final ItemFieldAccessor<String, FilterHolder> PMD_ACCESSOR = new ItemFieldAccessorAdapter<String, FilterHolder>(
            Util.COMP_STR) {
        @Override
        public String valueFor(FilterHolder holder) {
            return holder.forPMD ? "Y" : "";
        }
    };

    public static final ItemFieldAccessor<String, FilterHolder> CPD_ACCESSOR = new ItemFieldAccessorAdapter<String, FilterHolder>(
            Util.COMP_STR) {
        @Override
        public String valueFor(FilterHolder holder) {
            return holder.forCPD ? "Y" : "";
        }
    };

    public static final ItemFieldAccessor<String, FilterHolder> PATTERN_ACCESSOR = new ItemFieldAccessorAdapter<String, FilterHolder>(
            Util.COMP_STR) {
        @Override
        public String valueFor(FilterHolder holder) {
            return holder.pattern;
        }
    };

    public static final ItemColumnDescriptor<String, FilterHolder> INCLUDE_DESCRIPTOR = new ItemColumnDescriptor<>("",
            "   Type", SWT.LEFT, 85, false, INCLUDE_ACCESSOR);
    public static final ItemColumnDescriptor<String, FilterHolder> PMD_DESCRIPTOR = new ItemColumnDescriptor<>("",
            "PMD", SWT.CENTER, 55, false, PMD_ACCESSOR);
    public static final ItemColumnDescriptor<String, FilterHolder> CPD_DESCRIPTOR = new ItemColumnDescriptor<>("",
            "CPD", SWT.CENTER, 55, false, CPD_ACCESSOR);
    public static final ItemColumnDescriptor<String, FilterHolder> PATTERN_DESCRIPTOR = new ItemColumnDescriptor<>("",
            "Pattern", SWT.LEFT, 55, true, PATTERN_ACCESSOR);

    @SuppressWarnings("rawtypes")
    public static final ItemColumnDescriptor[] VISIBLE_COLUMNS = new ItemColumnDescriptor[] { INCLUDE_DESCRIPTOR,
        // pmd, cpd,
        PATTERN_DESCRIPTOR };
}
