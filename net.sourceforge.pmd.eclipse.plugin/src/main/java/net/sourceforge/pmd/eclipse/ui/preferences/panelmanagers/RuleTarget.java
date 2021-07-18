/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.panelmanagers;

import net.sourceforge.pmd.Rule;

/**
 * Specifies an intended rule recipient.
 * 
 * @author Brian Remedios
 */
public interface RuleTarget {

    void rule(Rule theRule);
}
