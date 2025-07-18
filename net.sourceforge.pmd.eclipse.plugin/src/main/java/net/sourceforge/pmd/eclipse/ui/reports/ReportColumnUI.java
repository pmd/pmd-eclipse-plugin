/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.reports;

import org.eclipse.swt.SWT;

import net.sourceforge.pmd.eclipse.ui.ItemColumnDescriptor;
import net.sourceforge.pmd.eclipse.ui.ItemFieldAccessor;
import net.sourceforge.pmd.eclipse.ui.ItemFieldAccessorAdapter;
import net.sourceforge.pmd.renderers.Renderer;

/**
 *
 * @author Brian Remedios
 */
public final class ReportColumnUI {
    private ReportColumnUI() {
        // utility / constants class
    }

    public static final ItemFieldAccessor<String, Renderer> NAME_ACC = new ItemFieldAccessorAdapter<String, Renderer>(
            null) {
        @Override
        public String valueFor(Renderer renderer) {
            return renderer.getName();
        }
    };

    public static final ItemFieldAccessor<String, Renderer> DESCRIPTION_ACC = new ItemFieldAccessorAdapter<String, Renderer>(
            null) {
        @Override
        public String valueFor(Renderer renderer) {
            return renderer.getDescription();
        }
    };

    public static final ItemFieldAccessor<Boolean, Renderer> SHOW_SUPPRESSED_ACC = new ItemFieldAccessorAdapter<Boolean, Renderer>(
            null) {
        @Override
        public Boolean valueFor(Renderer renderer) {
            return renderer.isShowSuppressedViolations();
        }
    };

    public static final ItemFieldAccessor<String, Renderer> PROPERTIES_ACC = new ItemFieldAccessorAdapter<String, Renderer>(
            null) {
        @Override
        public String valueFor(Renderer renderer) {
            return ReportManager.asString(renderer.getPropertiesByPropertyDescriptor());
        }
    };

    public static final ItemColumnDescriptor<String, Renderer> NAME = new ItemColumnDescriptor<>("", "Name", SWT.LEFT,
            55, true, NAME_ACC);
    public static final ItemColumnDescriptor<String, Renderer> DESCRIPTION = new ItemColumnDescriptor<>("", "Format",
            SWT.LEFT, 99, true, DESCRIPTION_ACC);
    public static final ItemColumnDescriptor<Boolean, Renderer> SUPPRESSED = new ItemColumnDescriptor<>("",
            "Show suppressed", SWT.LEFT, 40, true, SHOW_SUPPRESSED_ACC);
    public static final ItemColumnDescriptor<String, Renderer> PROPERTIES = new ItemColumnDescriptor<>("", "Properties",
            SWT.LEFT, 99, true, PROPERTIES_ACC);

    @SuppressWarnings("rawtypes")
    public static final ItemColumnDescriptor[] VISIBLE_COLUMNS = new ItemColumnDescriptor[] { NAME,
        /* suppressed, */ PROPERTIES };
}
