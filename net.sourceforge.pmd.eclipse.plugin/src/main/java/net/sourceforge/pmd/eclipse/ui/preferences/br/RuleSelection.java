/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.eclipse.ui.preferences.panelmanagers.Configuration;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.lang.rule.RulePriority;
import net.sourceforge.pmd.lang.rule.RuleReference;
import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.lang.rule.xpath.XPathRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;

/**
 * Represents a set of selected rules in a rule selection widget. Provides
 * useful metrics and determines common properties (if any).
 *
 * @author Brian Remedios
 */
public class RuleSelection implements RuleCollection {

    private Object[] ruleItems;

    public RuleSelection(Rule soleRule) {
        this(new Object[] { soleRule });
    }

    public RuleSelection(Object[] theRuleItems) {
        ruleItems = theRuleItems;
    }

    @Override
    public boolean isEmpty() {
        return ruleItems == null || ruleItems.length == 0;
    }

    public void soleRule(Rule theRule) {
        ruleItems = new Object[] { theRule };
    }

    /**
     * Iterate through all the rules ultimately held by the receiver. Returns
     * true if it went through every one, false if it stopped along the way.
     * 
     * @param visitor
     * @return
     */
    @Override
    public boolean rulesDo(RuleVisitor visitor) {

        for (Object item : ruleItems) {
            if (item instanceof Rule) {
                if (!visitor.accept((Rule) item)) {
                    return false;
                }
                continue;
            }
            if (item instanceof RuleGroup) {
                if (!((RuleGroup) item).rulesDo(visitor)) {
                    return false;
                }
            }
        }

        return true;
    }

    public ImplementationType implementationType() {

        if (ruleItems == null || ruleItems.length == 0) {
            return ImplementationType.Mixed;
        }

        final Set<ImplementationType> types = new HashSet<>();

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                types.add(implementationType(rule));
                return types.size() < 2;
            }
        };

        rulesDo(visitor);

        return types.size() > 1 ? ImplementationType.Mixed : types.iterator().next();
    }

    /**
     * Returns whether all the elements match by equality and position including
     * any possible children they may have.
     *
     * @param thisArray
     *            Object[]
     * @param thatArray
     *            Object[]
     * @return boolean
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public static final boolean valuesAreTransitivelyEqual(Object[] thisArray, Object[] thatArray) {
        if (thisArray == thatArray) {
            return true;
        }
        if (thisArray == null || thatArray == null) {
            return false;
        }
        if (thisArray.length != thatArray.length) {
            return false;
        }
        for (int i = 0; i < thisArray.length; i++) {
            if (!Objects.deepEquals(thisArray[i], thatArray[i])) {
                return false; // recurse if req'd
            }
        }
        return true;
    }

    public boolean haveDefaultValues() {
        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                return rule.getOverriddenPropertyDescriptors().isEmpty();
            }
        };

        return rulesDo(visitor);
    }

    public boolean hasOneRule() {
        if (ruleItems == null || ruleItems.length > 1) {
            return false;
        }
        return allRules().size() == 1;
    }

    public boolean hasMultipleRules() {
        return ruleItems != null && allRules().size() > 1;
    }

    @Override
    public Rule soleRule() {
        if (ruleItems == null || ruleItems.length != 1) {
            return null;
        }
        if (ruleItems[0] instanceof Rule) {
            return (Rule) ruleItems[0];
        }
        if (ruleItems[0] instanceof RuleGroup) {
            return ((RuleGroup) ruleItems[0]).soleRule();
        }

        return null; // should not get here
    }

    public Collection<String> ruleGroupNames() {
        if (ruleItems == null) {
            return Collections.emptyList();
        }

        Collection<String> names = new ArrayList<>();
        for (Object item : ruleItems) {
            if (item instanceof RuleGroup) {
                names.add(((RuleGroup) item).label());
            }
        }

        return names;
    }

    private static void useDefaultValues(Rule rule) {
        for (Map.Entry<PropertyDescriptor<?>, Object> entry : Configuration.filteredPropertiesOf(rule).entrySet()) {
            rule.setProperty((PropertyDescriptor) entry.getKey(), entry.getKey().defaultValue());
        }
    }

    public void useDefaultValues() {
        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                useDefaultValues(rule);
                return true;
            }
        };

        rulesDo(visitor);
    }

    public static ImplementationType implementationType(Rule rule) {
        if (rule instanceof RuleReference) {
            return ((RuleReference) rule).getRule() instanceof XPathRule ? ImplementationType.XPath
                    : ImplementationType.Java;
        } else {
            return rule instanceof XPathRule ? ImplementationType.XPath : ImplementationType.Java;
        }
    }

    public static <T> String commonStringValueFor(Object item, PropertyDescriptor<T> desc) {
        if (item instanceof Rule) {
            T value = ((Rule) item).getProperty(desc);
            return desc.serializer().toString(value);
        }
        return ((RuleGroup) item).commonStringProperty(desc);
    }

    public void setLanguage(final Language language) {
        if (ruleItems == null) {
            return;
        }

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                rule.setLanguage(language);
                return true;
            }
        };

        rulesDo(visitor);
    }

    public void setMinLanguageVersion(final LanguageVersion version) {
        if (ruleItems == null) {
            return;
        }

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                rule.setMinimumLanguageVersion(version);
                return true;
            }
        };

        rulesDo(visitor);
    }

    public void setMaxLanguageVersion(final LanguageVersion version) {
        if (ruleItems == null) {
            return;
        }

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                rule.setMaximumLanguageVersion(version);
                return true;
            }
        };

        rulesDo(visitor);
    }

    public void setPriority(final RulePriority priority) {
        if (ruleItems == null) {
            return;
        }

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                rule.setPriority(priority);
                return true;
            }
        };

        rulesDo(visitor);
    }

    public int removeAllFrom(RuleSet ruleSet) {
        List<Rule> rules = allRules();
        if (rules.isEmpty()) {
            return 0;
        }

        Set<Rule> rulesAsSet = new HashSet<>();
        rulesAsSet.addAll(rules);

        Iterator<Rule> currentRuleIter = ruleSet.getRules().iterator();

        int removed = 0;
        while (currentRuleIter.hasNext()) {
            // could be rule or a ruleReference
            Rule curRule = currentRuleIter.next(); 
            // if (curRule instanceof RuleReference) {
            // RuleReference rr = (RuleReference)curRule;
            // curRule = rr.getRule();
            // }
            if (rulesAsSet.contains(curRule)) {
                currentRuleIter.remove();
                removed++;
            }
        }

        return removed;
    }

    public List<Rule> allRules() {
        if (ruleItems == null || ruleItems.length == 0) {
            return Collections.emptyList();
        }

        final List<Rule> rules = new ArrayList<>(ruleItems.length);

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                rules.add(rule);
                return true;
            }
        };

        rulesDo(visitor);

        return rules;
    }

    public String commonStringValue(PropertyDescriptor<?> desc) {
        if (ruleItems == null || ruleItems.length == 0 || desc == null) {
            return null;
        }

        String value = commonStringValueFor(ruleItems[0], desc);
        if (value == null) {
            return null;
        }

        for (int i = 1; i < ruleItems.length; i++) {
            if (StringUtils.equals(StringUtils.stripToNull(value),
                    StringUtils.stripToNull(commonStringValueFor(ruleItems[i], desc)))) {
                return null;
            }
        }

        return value;
    }

    public <T> void setValue(final PropertyDescriptor<T> desc, final String value) {
        if (ruleItems == null || ruleItems.length == 0) {
            return;
        }

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                rule.setProperty(desc, desc.serializer().fromString(value));
                return true;
            }
        };

        rulesDo(visitor);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Collection<String> rgNames = ruleGroupNames();
        if (!rgNames.isEmpty()) {
            sb.append("groups: ").append(rgNames.size());
        }

        List<Rule> rulz = allRules();
        if (!rulz.isEmpty()) {
            sb.append(" rules: ").append(rulz.size());
        }
        return sb.toString();
    }
}
