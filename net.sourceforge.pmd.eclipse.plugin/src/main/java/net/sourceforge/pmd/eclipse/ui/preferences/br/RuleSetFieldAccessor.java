
package net.sourceforge.pmd.eclipse.ui.preferences.br;

import net.sourceforge.pmd.RuleSet;

/**
 * A value and label extractor interface for anything implementing the RuleSet
 * interface and may be real fields or values held as properties.
 *
 * Value returned are typed as comparable to facilitate sorting. Never return
 * null, return an empty string instead.
 *
 * @author Brian Remedios
 */
public interface RuleSetFieldAccessor {

    RuleSetFieldAccessor NAME = new BasicRuleSetFieldAccessor() {
        public Comparable<String> valueFor(RuleSet ruleSet) {
            return ruleSet.getName();
        }
    };

    RuleSetFieldAccessor FILE_NAME = new BasicRuleSetFieldAccessor() {
        public Comparable<String> valueFor(RuleSet ruleSet) {
            return ruleSet.getFileName();
        }
    };

    RuleSetFieldAccessor DESCRIPTION = new BasicRuleSetFieldAccessor() {
        public Comparable<String> valueFor(RuleSet ruleSet) {
            return ruleSet.getDescription();
        }
    };

    RuleSetFieldAccessor SIZE = new BasicRuleSetFieldAccessor() {
        public Comparable<Integer> valueFor(RuleSet ruleSet) {
            return ruleSet.size();
        }
    };

    RuleSetFieldAccessor INCLUDE_PATTERN_COUNT = new BasicRuleSetFieldAccessor() {
        public Comparable<Integer> valueFor(RuleSet ruleSet) {
            return ruleSet.getIncludePatterns().size();
        }
    };

    RuleSetFieldAccessor EXCLUDE_PATTERN_COUNT = new BasicRuleSetFieldAccessor() {
        public Comparable<Integer> valueFor(RuleSet ruleSet) {
            return ruleSet.getExcludePatterns().size();
        }
    };

    Comparable<?> valueFor(RuleSet ruleSet);

    // Comparable<?> valueFor(RuleSetCollection collection);

    // Set<Comparable<?>> uniqueValuesFor(RuleSetCollection collection);

    String labelFor(RuleSet ruleSet);

}
