/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.ast;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.lang.rule.AbstractRule;
import net.sourceforge.pmd.lang.rule.XPathRule;
import net.sourceforge.pmd.lang.rule.xpath.XPathVersion;

/**
 * 
 * @author Brian Remedios
 */
public final class XPathEvaluator {

    public static final XPathEvaluator INSTANCE = new XPathEvaluator();

    private XPathEvaluator() {
    }

    public Node getCompilationUnit(String source) {

        final List<Node> result = new ArrayList<>();

        PMDConfiguration configuration = new PMDConfiguration();
        configuration.setIgnoreIncrementalAnalysis(true);
        configuration.setForceLanguageVersion(getLanguageVersion());

        AbstractRule rule = new XPathRule(XPathVersion.XPATH_2_0, "") {
            @Override
            public void apply(List<? extends Node> nodes, RuleContext ctx) {
                result.addAll(nodes);
            }
        };
        RuleSet ruleset = RuleSetUtil.newSingle(rule);

        try (PmdAnalysis pmd = PmdAnalysis.create(configuration)) {
            pmd.addRuleSet(ruleset);
            pmd.files().addSourceFile(source, "[snippet]");
            pmd.performAnalysis();
        }

        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    private LanguageVersion getLanguageVersion() {
        return LanguageRegistry.getLanguage(JavaLanguageModule.NAME).getDefaultVersion();
    }

    /**
     * Builds a temporary XPathRule using the query provided and executes it
     * against the source. Returns a list of nodes detailing any issues found
     * with it.
     * 
     * @param source
     * @param xpathQuery
     * @param xpathVersion
     * @return
     */
    public List<Node> evaluate(String source, String xpathQuery, String xpathVersion) {

        final List<Node> results = new ArrayList<>();

        XPathRule xpathRule = new XPathRule(XPathVersion.ofId(xpathVersion), xpathQuery) {
            @Override
            public void addViolation(Object data, Node node, String arg) {
                results.add(node);
            }
        };

        xpathRule.setMessage("");
        xpathRule.setLanguage(getLanguageVersion().getLanguage());

        RuleSet ruleSet = RuleSetUtil.newSingle(xpathRule);

        PMDConfiguration configuration = new PMDConfiguration();
        configuration.setIgnoreIncrementalAnalysis(true);
        configuration.setForceLanguageVersion(getLanguageVersion());
        try (PmdAnalysis pmd = PmdAnalysis.create(configuration)) {
            pmd.addRuleSet(ruleSet);
            pmd.files().addSourceFile(source, "[snippet]");
            pmd.performAnalysis();
        }

        return results;
    }
}
