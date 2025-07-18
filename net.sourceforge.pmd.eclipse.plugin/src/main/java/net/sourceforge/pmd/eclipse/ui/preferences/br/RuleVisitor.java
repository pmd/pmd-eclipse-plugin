/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import net.sourceforge.pmd.lang.rule.Rule;

/**
 * 
 * @author Brian Remedios
 */
public interface RuleVisitor {

    /**
     * Process the rule provided and return whether to continue processing other
     * rules.
     * 
     * @param rule
     * @return boolean
     */
    boolean accept(Rule rule);
}
