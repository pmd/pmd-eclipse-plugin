/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.util.internal;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRule;
import net.sourceforge.pmd.reporting.RuleContext;

public class SpyingRule extends AbstractRule {

    private static Node rootNode;

    @Override
    public void apply(Node node, RuleContext ctx) {
        rootNode = node;
    }

    public Node getRootNode() {
        return rootNode;
    }
}
