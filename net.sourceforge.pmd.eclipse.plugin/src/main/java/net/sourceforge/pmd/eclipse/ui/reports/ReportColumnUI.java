/**
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
public interface ReportColumnUI {

    ItemFieldAccessor<String, Renderer> NAME_ACC = new ItemFieldAccessorAdapter<String, Renderer>(null) {
        @Override
        public String valueFor(Renderer renderer) {
            return renderer.getName();
        }
    };

    ItemFieldAccessor<String, Renderer> DESCRIPTION_ACC = new ItemFieldAccessorAdapter<String, Renderer>(null) {
        @Override
        public String valueFor(Renderer renderer) {
            return renderer.getDescription();
        }
    };

    ItemFieldAccessor<Boolean, Renderer> SHOW_SUPPRESSED_ACC = new ItemFieldAccessorAdapter<Boolean, Renderer>(null) {
        @Override
        public Boolean valueFor(Renderer renderer) {
            return renderer.isShowSuppressedViolations();
        }
    };

    ItemFieldAccessor<String, Renderer> PROPERTIES_ACC = new ItemFieldAccessorAdapter<String, Renderer>(null) {
        @Override
        public String valueFor(Renderer renderer) {
            return ReportManager.asString(renderer.getPropertiesByPropertyDescriptor());
        }
    };

    ItemColumnDescriptor<String, Renderer> NAME = new ItemColumnDescriptor<String, Renderer>("", "Name", SWT.LEFT, 55,
            true, NAME_ACC);
    ItemColumnDescriptor<String, Renderer> DESCRIPTION = new ItemColumnDescriptor<String, Renderer>("", "Format",
            SWT.LEFT, 99, true, DESCRIPTION_ACC);
    ItemColumnDescriptor<Boolean, Renderer> SUPPRESSED = new ItemColumnDescriptor<Boolean, Renderer>("",
            "Show suppressed", SWT.LEFT, 40, true, SHOW_SUPPRESSED_ACC);
    ItemColumnDescriptor<String, Renderer> PROPERTIES = new ItemColumnDescriptor<String, Renderer>("", "Properties",
            SWT.LEFT, 99, true, PROPERTIES_ACC);

    @SuppressWarnings("rawtypes")
    ItemColumnDescriptor[] VISIBLE_COLUMNS = new ItemColumnDescriptor[] { NAME, /* suppressed, */ PROPERTIES };
}
