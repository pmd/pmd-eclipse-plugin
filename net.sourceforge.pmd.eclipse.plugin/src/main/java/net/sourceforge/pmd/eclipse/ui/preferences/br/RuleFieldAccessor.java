/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.eclipse.plugin.UISettings;
import net.sourceforge.pmd.eclipse.ui.quickfix.PMDResolutionGenerator;
import net.sourceforge.pmd.eclipse.util.Util;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersion;

/**
 * A value and label extractor interface for anything implementing the Rule
 * interface and may be real fields or values held as properties.
 *
 * Value returned are typed as comparable to facilitate sorting. Never return
 * null, return an empty string instead.
 *
 * @author Brian Remedios
 */
public interface RuleFieldAccessor {

    // NOTE: If you update these values then you also need to update
    // the tooltip that references them:
    // 'preference.ruleset.column.rule_type.tooltip'

    String[] RULE_TYPE_GENERIC = new String[] { "-", "Generic" };
    String[] RULE_TYPE_XPATH = new String[] { "X", "XPath" };
    String[] RULE_TYPE_DATAFLOW = new String[] { "D", "Dataflow" };
    String[] RULE_TYPE_TYPERESOLUTION = new String[] { "T", "Type resolving" };

    RuleFieldAccessor SINCE = new BasicRuleFieldAccessor() {
        public Comparable<String> valueFor(Rule rule) {
            return rule.getSince();
        }
    };

    RuleFieldAccessor PRIORITY = new BasicRuleFieldAccessor() {
        public Comparable<?> valueFor(Rule rule) {
            return rule.getPriority();
        }

        public String labelFor(Rule rule) {
            return UISettings.labelFor(rule.getPriority());
        }

        public Comparable<?> valueFor(RuleCollection collection) {
            return RuleUtil.commonPriority(collection);
        }
    };

    RuleFieldAccessor PRIORITY_NAME = new BasicRuleFieldAccessor() {
        public Comparable<String> valueFor(Rule rule) {
            return UISettings.labelFor(rule.getPriority());
        }
    };

    RuleFieldAccessor FIX_COUNT = new BasicRuleFieldAccessor() {
        public Comparable<Integer> valueFor(Rule rule) {
            return PMDResolutionGenerator.fixCountFor(rule);
        }
    };

    RuleFieldAccessor NAME = new BasicRuleFieldAccessor() {
        public Comparable<String> valueFor(Rule rule) {
            return rule.getName();
        }
    };

    RuleFieldAccessor DESCRIPTION = new BasicRuleFieldAccessor() {
        public Comparable<String> valueFor(Rule rule) {
            return rule.getDescription();
        }
    };

    RuleFieldAccessor USES_DFA = new BasicRuleFieldAccessor() {
        public Comparable<Boolean> valueFor(Rule rule) {
            return rule.isDfa();
        }

        public Comparable<?> valueFor(RuleCollection collection) {
            return RuleUtil.allUseDfa(collection);
        }
    };

    RuleFieldAccessor MESSAGE = new BasicRuleFieldAccessor() {
        public Comparable<String> valueFor(Rule rule) {
            return rule.getMessage();
        }
    };

    RuleFieldAccessor URL = new BasicRuleFieldAccessor() {
        public Comparable<String> valueFor(Rule rule) {
            return rule.getExternalInfoUrl();
        }
    };

    RuleFieldAccessor EXAMPLE_COUNT = new BasicRuleFieldAccessor() {
        public Comparable<?> valueFor(Rule rule) {
            int count = rule.getExamples().size();
            return count > 0 ? Integer.toString(count) : "";
        }
    };

    RuleFieldAccessor RULE_TYPE = new BasicRuleFieldAccessor() {
        public Comparable<String> valueFor(Rule rule) {
            StringBuilder sb = new StringBuilder(3);
            if (RuleUtil.isXPathRule(rule)) {
                sb.append(RULE_TYPE_XPATH[0]);
            }
            if (rule.isDfa()) {
                sb.append(RULE_TYPE_DATAFLOW[0]);
            }
            if (rule.isTypeResolution()) {
                sb.append(RULE_TYPE_TYPERESOLUTION[0]);
            }
            if (sb.length() == 0) {
                sb.append(RULE_TYPE_GENERIC[0]);
            }
            return sb.toString();
        }

        final int labelTypeIdx = 0; // just show the letter codes

        public String labelFor(Rule rule) {
            List<String> types = new ArrayList<String>(3);
            if (RuleUtil.isXPathRule(rule)) {
                types.add(RULE_TYPE_XPATH[labelTypeIdx]);
            }
            // if (if (RuleUtil.isXPathRule(rule)) TODO
            if (rule.isDfa()) {
                types.add(RULE_TYPE_DATAFLOW[labelTypeIdx]);
            }
            if (rule.isTypeResolution()) {
                types.add(RULE_TYPE_TYPERESOLUTION[labelTypeIdx]);
            }
            if (types.isEmpty()) {
                types.add(RULE_TYPE_GENERIC[labelTypeIdx]);
            }
            return Util.asString(types, ", ");
        }
    };

    RuleFieldAccessor LANGUAGE = new BasicRuleFieldAccessor() {
        public Comparable<Language> valueFor(Rule rule) {
            return rule.getLanguage();
        }

        public String labelFor(Rule rule) {
            return rule.getLanguage().getName();
        }
    };

    RuleFieldAccessor MIN_LANGUAGE_VERSION = new BasicRuleFieldAccessor() {
        public Comparable<LanguageVersion> valueFor(Rule rule) {
            return rule.getMinimumLanguageVersion();
        }
    };

    RuleFieldAccessor MAX_LANGUAGE_VERSION = new BasicRuleFieldAccessor() {
        public Comparable<LanguageVersion> valueFor(Rule rule) {
            return rule.getMaximumLanguageVersion();
        }
    };

    RuleFieldAccessor VIOLATION_REGEX = new BasicRuleFieldAccessor() {
        public Comparable<String> valueFor(Rule rule) {
            return rule.getProperty(Rule.VIOLATION_SUPPRESS_REGEX_DESCRIPTOR);
        }
    };

    RuleFieldAccessor VIOLATION_XPATH = new BasicRuleFieldAccessor() {
        public Comparable<String> valueFor(Rule rule) {
            return rule.getProperty(Rule.VIOLATION_SUPPRESS_XPATH_DESCRIPTOR);
        }
    };

    RuleFieldAccessor NON_DEFAULT_PROPERTY_COUNT = new BasicRuleFieldAccessor() {
        public Comparable<Integer> valueFor(Rule rule) {
            return RuleUtil.modifiedPropertiesIn(rule).size();
        }
    };

    /**
     * @param rule
     *            Rule
     * @return Comparable
     */
    Comparable<?> valueFor(Rule rule);

    Comparable<?> valueFor(RuleCollection collection);

    Set<Comparable<?>> uniqueValuesFor(RuleCollection collection);

    String labelFor(Rule rule);

}
