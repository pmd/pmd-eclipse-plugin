/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.ast;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.eclipse.ui.actions.RuleSetUtil;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.Parser;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.lang.java.ast.ParseException;
import net.sourceforge.pmd.lang.rule.XPathRule;

/**
 * 
 * @author Brian Remedios
 */
public final class XPathEvaluator {

    public static final XPathEvaluator INSTANCE = new XPathEvaluator();

    private XPathEvaluator() {
    }

    public Node getCompilationUnit(String source) {

        LanguageVersionHandler languageVersionHandler = getLanguageVersionHandler();
        Parser parser = languageVersionHandler.getParser(languageVersionHandler.getDefaultParserOptions());
        Node node = parser.parse(null, new StringReader(source));
        languageVersionHandler.getSymbolFacade().start(node);
        languageVersionHandler.getTypeResolutionFacade(null).start(node);
        return node;
    }

    private LanguageVersionHandler getLanguageVersionHandler() {
        LanguageVersion languageVersion = getLanguageVersion();
        return languageVersion.getLanguageVersionHandler();
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
     * @throws ParseException
     */
    public List<Node> evaluate(String source, String xpathQuery, String xpathVersion) throws ParseException {
        Node c = getCompilationUnit(source);

        final List<Node> results = new ArrayList<>();

        XPathRule xpathRule = new XPathRule() {
            @Override
            public void addViolation(Object data, Node node, String arg) {
                results.add(node);
            }
        };

        xpathRule.setMessage("");
        xpathRule.setLanguage(getLanguageVersion().getLanguage());
        xpathRule.setProperty(XPathRule.XPATH_DESCRIPTOR, xpathQuery);
        xpathRule.setProperty(XPathRule.VERSION_DESCRIPTOR, xpathVersion);

        RuleSet ruleSet = RuleSetUtil.newSingle(xpathRule);

        RuleContext ruleContext = new RuleContext();
        ruleContext.setLanguageVersion(getLanguageVersion());

        List<Node> nodes = new ArrayList<>(1);
        nodes.add(c);

        ruleSet.apply(nodes, ruleContext);

        return results;
    }
}
