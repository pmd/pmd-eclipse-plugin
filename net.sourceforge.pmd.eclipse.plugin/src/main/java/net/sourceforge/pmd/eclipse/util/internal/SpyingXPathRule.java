/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.util.internal;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.XPathRule;
import net.sourceforge.pmd.lang.rule.xpath.XPathVersion;

public class SpyingXPathRule extends XPathRule {

    private static List<Node> result = new ArrayList<>();

    @SuppressWarnings("deprecation")
    public SpyingXPathRule() {
        // default constructor required for deep copy
    }

    public SpyingXPathRule(XPathVersion version, String expression, Language language) {
        super(version, expression);
        setMessage("");
        setLanguage(language);
    }

    @Override
    public void addViolation(Object data, Node node, String arg) {
        result.add(node);
    }

    public List<Node> getResult() {
        return result;
    }
}
