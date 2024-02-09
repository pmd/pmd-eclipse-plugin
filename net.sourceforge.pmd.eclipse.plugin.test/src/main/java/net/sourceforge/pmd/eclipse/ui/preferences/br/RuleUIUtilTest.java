/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import net.sourceforge.pmd.eclipse.ui.IndexedString;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.lang.rule.xpath.XPathRule;
import net.sourceforge.pmd.lang.rule.xpath.XPathVersion;

public class RuleUIUtilTest {

    /**
     * There should be no UnsupportedOperationException...
     */
    @Test
    public void testTndexedPropertyStringFromRule() {
        Rule rule = new XPathRule(XPathVersion.XPATH_2_0, "");
        IndexedString s = RuleUIUtil.indexedPropertyStringFrom(rule);
        assertNotNull(s);
    }
}
