/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetReference;
import net.sourceforge.pmd.lang.rule.RuleReference;

/**
 * 
 * @author Brian Remedios
 */
public class RuleSetUtil {

    private RuleSetUtil() {
    }

    public static RuleSet newCopyOf(RuleSet original) {
        RuleSetFactory factory = new RuleSetFactory();
        return factory.createRuleSetCopy(original);
    }

    public static final String DEFAULT_RULESET_NAME = "pmd-eclipse";
    public static final String DEFAULT_RULESET_DESCRIPTION = "PMD Plugin preferences rule set";

    /**
     * This should not really work but the ruleset hands out its internal
     * container....oops! :)
     * 
     * @param ruleSet
     * @param wantedRuleNames
     * @return
     */
    public static RuleSet retainOnly(RuleSet ruleSet, Collection<Rule> wantedRules) {
        RuleSetFactory factory = new RuleSetFactory();
        return factory.createNewRuleSet(ruleSet.getName(), ruleSet.getDescription(), ruleSet.getFileName(),
                ruleSet.getExcludePatterns(), ruleSet.getIncludePatterns(), wantedRules);
    }

    /**
     * Removes the rule with the same name from the ruleset.
     * 
     * @param ruleSet
     * @param removedRule
     * @return
     */
    public static RuleSet removeRule(RuleSet ruleSet, Rule removedRule) {
        List<Rule> wantedRules = new ArrayList<Rule>(ruleSet.getRules());
        wantedRules.remove(removedRule);
        return retainOnly(ruleSet, wantedRules);
    }

    public static RuleSet addExcludePatterns(RuleSet ruleSet, Collection<String> activeExclusionPatterns,
            Collection<String> buildPathExcludePatterns) {
        Set<String> newExcludePatterns = new HashSet<String>(ruleSet.getExcludePatterns());
        newExcludePatterns.addAll(activeExclusionPatterns);
        newExcludePatterns.addAll(buildPathExcludePatterns);
        Set<String> newIncludePatterns = new HashSet<String>(ruleSet.getIncludePatterns());

        RuleSetFactory factory = new RuleSetFactory();
        return factory.createNewRuleSet(ruleSet.getName(), ruleSet.getDescription(), ruleSet.getFileName(),
                newExcludePatterns, newIncludePatterns, ruleSet.getRules());
    }

    public static RuleSet addIncludePatterns(RuleSet ruleSet, Collection<String> activeInclusionPatterns,
            Collection<String> buildPathIncludePatterns) {
        Set<String> newExcludePatterns = new HashSet<String>(ruleSet.getExcludePatterns());
        Set<String> newIncludePatterns = new HashSet<String>(ruleSet.getIncludePatterns());
        newIncludePatterns.addAll(activeInclusionPatterns);
        newIncludePatterns.addAll(buildPathIncludePatterns);

        RuleSetFactory factory = new RuleSetFactory();
        return factory.createNewRuleSet(ruleSet.getName(), ruleSet.getDescription(), ruleSet.getFileName(),
                newExcludePatterns, newIncludePatterns, ruleSet.getRules());
    }

    public static RuleSet newSingle(Rule rule) {
        RuleSetFactory factory = new RuleSetFactory();
        return factory.createSingleRuleRuleSet(rule);
    }

    public static RuleSet newEmpty(String name, String description) {
        RuleSetFactory factory = new RuleSetFactory();
        Set<String> emptySet = Collections.emptySet();
        Set<Rule> emptyRules = Collections.emptySet();
        return factory.createNewRuleSet(name, description, null, emptySet, emptySet, emptyRules);
    }

    public static RuleSet addRuleSetByReference(RuleSet ruleSet, RuleSet sourceRuleSet, boolean allRules) {
        RuleSetReference reference = new RuleSetReference(sourceRuleSet.getFileName(), allRules);
        Collection<Rule> rules = new ArrayList<Rule>(ruleSet.getRules());
        for (Rule rule : sourceRuleSet.getRules()) {
            RuleReference ruleRef = new RuleReference(rule, reference);
            rules.add(ruleRef);
        }
        RuleSetFactory factory = new RuleSetFactory();
        return factory.createNewRuleSet(ruleSet.getName(), ruleSet.getDescription(), ruleSet.getFileName(),
                ruleSet.getExcludePatterns(), ruleSet.getIncludePatterns(), rules);
    }

    public static RuleSet addRules(RuleSet ruleSet, Collection<Rule> newRules) {
        RuleSetFactory factory = new RuleSetFactory();
        Collection<Rule> allRules = new ArrayList<Rule>();
        allRules.addAll(ruleSet.getRules());
        allRules.addAll(newRules);
        return factory.createNewRuleSet(ruleSet.getName(), ruleSet.getDescription(), ruleSet.getFileName(),
                ruleSet.getExcludePatterns(), ruleSet.getIncludePatterns(), allRules);
    }

    public static RuleSet addRule(RuleSet ruleSet, Rule newRule) {
        return addRules(ruleSet, Collections.singleton(newRule));
    }

    public static RuleSet setExcludePatterns(RuleSet ruleSet, Collection<String> excludePatterns) {
        RuleSetFactory factory = new RuleSetFactory();
        return factory.createNewRuleSet(ruleSet.getName(), ruleSet.getDescription(), ruleSet.getFileName(),
                excludePatterns, ruleSet.getIncludePatterns(), ruleSet.getRules());
    }

    public static RuleSet setIncludePatterns(RuleSet ruleSet, Collection<String> includePatterns) {
        RuleSetFactory factory = new RuleSetFactory();
        return factory.createNewRuleSet(ruleSet.getName(), ruleSet.getDescription(), ruleSet.getFileName(),
                ruleSet.getExcludePatterns(), includePatterns, ruleSet.getRules());
    }

    public static RuleSet setNameDescription(RuleSet ruleSet, String name, String description) {
        RuleSetFactory factory = new RuleSetFactory();
        return factory.createNewRuleSet(name, description, ruleSet.getFileName(), ruleSet.getExcludePatterns(),
                ruleSet.getIncludePatterns(), ruleSet.getRules());
    }

    public static RuleSet setFileName(RuleSet ruleSet, String fileName) {
        RuleSetFactory factory = new RuleSetFactory();
        return factory.createNewRuleSet(ruleSet.getName(), ruleSet.getDescription(), fileName,
                ruleSet.getExcludePatterns(), ruleSet.getIncludePatterns(), ruleSet.getRules());
    }

    public static RuleSet addExcludePatterns(RuleSet rs, Collection<String> excludePatterns) {
        return addExcludePatterns(rs, excludePatterns, new HashSet<String>());
    }

    public static RuleSet addIncludePatterns(RuleSet rs, Collection<String> includePatterns) {
        return addIncludePatterns(rs, includePatterns, new HashSet<String>());
    }

    public static RuleSet clearRules(RuleSet ruleSet) {
        RuleSetFactory factory = new RuleSetFactory();
        Set<Rule> emptySet = Collections.emptySet();
        return factory.createNewRuleSet(ruleSet.getName(), ruleSet.getDescription(), ruleSet.getFileName(),
                ruleSet.getExcludePatterns(), ruleSet.getIncludePatterns(), emptySet);
    }

    public static Rule findSameRule(Collection<Rule> haystack, Rule search) {
        for (Rule rule : haystack) {
            if (rule == search
                    || rule.getName().equals(search.getName()) && rule.getLanguage() == search.getLanguage()) {
                return rule;
            }
        }
        return null;
    }
}
