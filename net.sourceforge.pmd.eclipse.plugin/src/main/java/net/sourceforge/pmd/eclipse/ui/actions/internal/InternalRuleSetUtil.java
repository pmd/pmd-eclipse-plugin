/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.ui.actions.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RulesetsFactoryUtils;

public class InternalRuleSetUtil {
    private InternalRuleSetUtil() {}

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

    public static RuleSet addFileExclusions(RuleSet rs, Collection<Pattern> excludePatterns) {
        return addExcludePatterns(rs, excludePatterns, new HashSet<Pattern>());
    }

    public static RuleSet addFileInclusions(RuleSet rs, Collection<Pattern> includePatterns) {
        return addIncludePatterns(rs, includePatterns, new HashSet<Pattern>());
    }

    public static RuleSet addExcludePatterns(RuleSet ruleSet, Collection<Pattern> activeExclusionPatterns,
            Collection<Pattern> buildPathExcludePatterns) {
        Set<Pattern> newExcludePatterns = new HashSet<Pattern>(ruleSet.getFileExclusions());
        newExcludePatterns.addAll(activeExclusionPatterns);
        newExcludePatterns.addAll(buildPathExcludePatterns);
        Set<Pattern> newIncludePatterns = new HashSet<Pattern>(ruleSet.getFileInclusions());

        RuleSetFactory factory = RulesetsFactoryUtils.defaultFactory();
        return factory.createNewRuleSet(ruleSet.getName(), ruleSet.getDescription(), ruleSet.getFileName(),
                convert(newExcludePatterns),
                convert(newIncludePatterns),
                ruleSet.getRules());
    }

    public static RuleSet addIncludePatterns(RuleSet ruleSet, Collection<Pattern> activeInclusionPatterns,
            Collection<Pattern> buildPathIncludePatterns) {
        Set<Pattern> newExcludePatterns = new HashSet<Pattern>(ruleSet.getFileExclusions());
        Set<Pattern> newIncludePatterns = new HashSet<Pattern>(ruleSet.getFileInclusions());
        newIncludePatterns.addAll(activeInclusionPatterns);
        newIncludePatterns.addAll(buildPathIncludePatterns);

        RuleSetFactory factory = RulesetsFactoryUtils.defaultFactory();
        return factory.createNewRuleSet(ruleSet.getName(), ruleSet.getDescription(), ruleSet.getFileName(),
                convert(newExcludePatterns),
                convert(newIncludePatterns),
                ruleSet.getRules());
    }

    public static Collection<String> convert(Collection<Pattern> patterns) {
        Collection<String> result = new HashSet<String>();
        for (Pattern p : patterns) {
            result.add(p.pattern());
        }
        return result;
    }

}
