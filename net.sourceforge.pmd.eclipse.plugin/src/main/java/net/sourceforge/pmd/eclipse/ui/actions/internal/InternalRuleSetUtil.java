/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.ui.actions.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
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

    public static Collection<Pattern> convertStringPatterns(Collection<String> patterns) {
        Collection<Pattern> result = new HashSet<Pattern>();
        for (String p : patterns) {
            result.add(Pattern.compile(p));
        }
        return result;
    }

    public static boolean ruleSetsApplies(List<RuleSet> rulesets, File file) {
        for (RuleSet ruleSet : rulesets) {
            if (ruleSet.applies(file)) {
                return true;
            }
        }
        return false;
    }

    public static int countRules(List<RuleSet> rulesets) {
        int rules = 0;
        for (RuleSet ruleset : rulesets) {
            rules += ruleset.size();
        }
        return rules;
    }

    public static Collection<Rule> allRules(List<RuleSet> rulesets) {
        Collection<Rule> result = new ArrayList<>();
        for (RuleSet ruleset : rulesets) {
            result.addAll(ruleset.getRules());
        }
        return result;
    }

    public static RuleSets toRuleSets(List<RuleSet> rulesets) {
        RuleSets result = new RuleSets();
        for (RuleSet ruleSet : rulesets) {
            result.addRuleSet(ruleSet);
        }
        return result;
    }

    public static RuleSetFactory createFactoryFromRuleSets(final List<RuleSet> rulesets) {
        return new RuleSetFactory() {
            @Override
            public RuleSets createRuleSets(String referenceString) throws RuleSetNotFoundException {
                return toRuleSets(rulesets);
            }
        };
    }
}
