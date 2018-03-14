
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
public interface FilterColumnUI {

    ItemFieldAccessor<String, FilterHolder> INCLUDE_ACCESSOR = new ItemFieldAccessorAdapter<String, FilterHolder>(null) {
        public Image imageFor(FilterHolder holder) {
            return FilterPreferencesPage.typeIconFor(holder);
        }
    };

    ItemFieldAccessor<String, FilterHolder> PMD_ACCESSOR = new ItemFieldAccessorAdapter<String, FilterHolder>(Util.compStr) {
        public String valueFor(FilterHolder holder) {
            return holder.forPMD ? "Y" : "";
        }
    };

    ItemFieldAccessor<String, FilterHolder> CPD_ACCESSOR = new ItemFieldAccessorAdapter<String, FilterHolder>(Util.compStr) {
        public String valueFor(FilterHolder holder) {
            return holder.forCPD ? "Y" : "";
        }
    };

    ItemFieldAccessor<String, FilterHolder> PATTERN_ACCESSOR = new ItemFieldAccessorAdapter<String, FilterHolder>(
            Util.compStr) {
        public String valueFor(FilterHolder holder) {
            return holder.pattern;
        }
    };

    ItemColumnDescriptor<String, FilterHolder> INCLUDE_DESCRIPTOR = new ItemColumnDescriptor<String, FilterHolder>("", "   Type",
            SWT.LEFT, 85, false, INCLUDE_ACCESSOR);
    ItemColumnDescriptor<String, FilterHolder> PMD_DESCRIPTOR = new ItemColumnDescriptor<String, FilterHolder>("", "PMD",
            SWT.CENTER, 55, false, PMD_ACCESSOR);
    ItemColumnDescriptor<String, FilterHolder> CPD_DESCRIPTOR = new ItemColumnDescriptor<String, FilterHolder>("", "CPD",
            SWT.CENTER, 55, false, CPD_ACCESSOR);
    ItemColumnDescriptor<String, FilterHolder> PATTERN_DESCRIPTOR = new ItemColumnDescriptor<String, FilterHolder>("", "Pattern",
            SWT.LEFT, 55, true, PATTERN_ACCESSOR);

    @SuppressWarnings("rawtypes")
    ItemColumnDescriptor[] VISIBLE_COLUMNS = new ItemColumnDescriptor[] { INCLUDE_DESCRIPTOR,
        // pmd, cpd,
        PATTERN_DESCRIPTOR };
}
