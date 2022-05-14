/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.util.internal;

import java.util.List;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRule;

public class SpyingRule extends AbstractRule {

    private static Node rootNode;

    @SuppressWarnings("deprecation")
    public SpyingRule() {
        setUsesDFA();
    }

    @Override
    public void apply(List<? extends Node> nodes, RuleContext ctx) {
        rootNode = nodes.get(0);
    }

    public Node getRootNode() {
        return rootNode;
    }
}
