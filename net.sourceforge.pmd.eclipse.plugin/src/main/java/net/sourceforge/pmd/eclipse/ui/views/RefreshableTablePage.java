/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views;

import org.eclipse.jface.viewers.TableViewer;

public interface RefreshableTablePage {

    TableViewer tableViewer();

    void refresh();
}
