/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences;

import net.sourceforge.pmd.lang.rule.Rule;

public interface RuleDupeChecker {

    boolean isDuplicate(Rule otherRule);
}
