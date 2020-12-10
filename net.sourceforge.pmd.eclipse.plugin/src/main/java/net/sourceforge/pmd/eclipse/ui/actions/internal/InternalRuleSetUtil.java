/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.ui.actions.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RulesetsFactoryUtils;

public class InternalRuleSetUtil {
    public static RuleSet setFileExclusions(RuleSet ruleSet, Collection<Pattern> excludePatterns) {
        RuleSetFactory factory = RulesetsFactoryUtils.defaultFactory();
        return factory.createNewRuleSet(ruleSet.getName(), ruleSet.getDescription(), ruleSet.getFileName(),
                convert(excludePatterns), convert(ruleSet.getFileInclusions()), ruleSet.getRules());
    }

    public static RuleSet setFileInclusions(RuleSet ruleSet, Collection<Pattern> includePatterns) {
        RuleSetFactory factory = RulesetsFactoryUtils.defaultFactory();
        return factory.createNewRuleSet(ruleSet.getName(), ruleSet.getDescription(), ruleSet.getFileName(),
                InternalRuleSetUtil.convert(ruleSet.getFileExclusions()),
                InternalRuleSetUtil.convert(includePatterns), ruleSet.getRules());
    }

    public static Collection<String> convert(Collection<Pattern> patterns) {
        Collection<String> result = new HashSet<String>();
        for (Pattern p : patterns) {
            result.add(p.pattern());
        }
        return result;
    }

}
