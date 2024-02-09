/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.properties;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.builder.MarkerUtil;
import net.sourceforge.pmd.eclipse.ui.dialogs.ViolationDetailsDialogPage;
import net.sourceforge.pmd.lang.rule.Rule;

public class PMDMarkerPropertyPage extends PropertyPage {

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();

        IMarker marker = (IMarker) getElement();
        Rule rule = PMDPlugin.getDefault().getPreferencesManager().getRuleSet()
                .getRuleByName(MarkerUtil.ruleNameFor(marker));

        Composite composite = WidgetFactory.composite(SWT.NONE).layout(new GridLayout())
                .layoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL))
                .create(parent);
        ViolationDetailsDialogPage content = new ViolationDetailsDialogPage(marker, rule);
        content.createControl(composite);
        return composite;
    }
}
