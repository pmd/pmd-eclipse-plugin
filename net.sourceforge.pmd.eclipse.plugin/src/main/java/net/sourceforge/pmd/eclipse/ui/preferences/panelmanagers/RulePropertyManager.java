/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.panelmanagers;

import org.eclipse.swt.widgets.TabItem;

import net.sourceforge.pmd.eclipse.ui.preferences.br.RuleSelection;

public interface RulePropertyManager {

    void tab(TabItem tab);

    boolean isActive();

    void manage(RuleSelection rules);

    boolean validate();

    void loadValues();
}
