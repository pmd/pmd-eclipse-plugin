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
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetLoader;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.lang.document.FileId;

public final class InternalRuleSetUtil {
    private InternalRuleSetUtil() {}

    public static RuleSet setFileExclusions(RuleSet ruleSet, Collection<Pattern> excludePatterns) {
        return RuleSet.create(ruleSet.getName(), ruleSet.getDescription(),
                ruleSet.getFileName(),
                excludePatterns,
                ruleSet.getFileInclusions(),
                ruleSet.getRules());
    }

    public static RuleSet setFileInclusions(RuleSet ruleSet, Collection<Pattern> includePatterns) {
        return RuleSet.create(ruleSet.getName(), ruleSet.getDescription(),
                ruleSet.getFileName(),
                ruleSet.getFileExclusions(),
                includePatterns,
                ruleSet.getRules());
    }

    public static RuleSet addFileExclusions(RuleSet rs, Collection<Pattern> excludePatterns) {
        return addExcludePatterns(rs, excludePatterns, new HashSet<>());
    }

    public static RuleSet addFileInclusions(RuleSet rs, Collection<Pattern> includePatterns) {
        return addIncludePatterns(rs, includePatterns, new HashSet<>());
    }

    public static RuleSet addExcludePatterns(RuleSet ruleSet, Collection<Pattern> activeExclusionPatterns,
            Collection<Pattern> buildPathExcludePatterns) {
        Set<Pattern> newExcludePatterns = new HashSet<>(ruleSet.getFileExclusions());
        newExcludePatterns.addAll(activeExclusionPatterns);
        newExcludePatterns.addAll(buildPathExcludePatterns);
        Set<Pattern> newIncludePatterns = new HashSet<>(ruleSet.getFileInclusions());

        return RuleSet.create(ruleSet.getName(), ruleSet.getDescription(),
                ruleSet.getFileName(),
                newExcludePatterns,
                newIncludePatterns,
                ruleSet.getRules());
    }

    public static RuleSet addIncludePatterns(RuleSet ruleSet, Collection<Pattern> activeInclusionPatterns,
            Collection<Pattern> buildPathIncludePatterns) {
        Set<Pattern> newExcludePatterns = new HashSet<>(ruleSet.getFileExclusions());
        Set<Pattern> newIncludePatterns = new HashSet<>(ruleSet.getFileInclusions());
        newIncludePatterns.addAll(activeInclusionPatterns);
        newIncludePatterns.addAll(buildPathIncludePatterns);

        return RuleSet.create(ruleSet.getName(), ruleSet.getDescription(),
                ruleSet.getFileName(),
                newExcludePatterns,
                newIncludePatterns,
                ruleSet.getRules());
    }

    public static Collection<String> convert(Collection<Pattern> patterns) {
        Collection<String> result = new HashSet<>();
        for (Pattern p : patterns) {
            result.add(p.pattern());
        }
        return result;
    }

    public static Collection<Pattern> convertStringPatterns(Collection<String> patterns) {
        Collection<Pattern> result = new HashSet<>();
        for (String p : patterns) {
            result.add(Pattern.compile(p));
        }
        return result;
    }

    public static boolean ruleSetsApplies(List<RuleSet> rulesets, File file) {
        FileId fileId = FileId.fromPathLikeString(file.toString());
        for (RuleSet ruleSet : rulesets) {
            if (ruleSet.applies(fileId)) {
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
        return new RuleSets(rulesets);
    }

    public static RuleSetLoader getDefaultRuleSetLoader() {
        return new RuleSetLoader()
                .enableCompatibility(true)
                .warnDeprecated(false)
                .filterAbovePriority(RulePriority.LOW);
    }
}
