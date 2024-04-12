/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.sourceforge.pmd.eclipse.ui.preferences.panelmanagers.Configuration;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.lang.rule.RulePriority;
import net.sourceforge.pmd.lang.rule.RuleReference;
import net.sourceforge.pmd.lang.rule.xpath.XPathRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * 
 * @author Brian Remedios
 */
public final class RuleUtil {

    private RuleUtil() {
    }

    public static boolean isDefaultValue(Map.Entry<PropertyDescriptor<?>, Object> entry) {
        PropertyDescriptor<?> desc = entry.getKey();
        Object value = entry.getValue();
        return areEqual(desc.defaultValue(), value);
    }

    public static boolean isXPathRule(PropertySource source) {
        // Testing for property descriptor XPathRule.XPATH_DESCRIPTOR ("xpath")
        // is not reliable, since this property might not be existing yet
        return source instanceof XPathRule
                || source instanceof RuleReference && ((RuleReference) source).getRule() instanceof XPathRule;
    }

    // TODO move elsewhere
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public static boolean areEqual(Object value, Object otherValue) {
        if (value == otherValue) {
            return true;
        }
        if (value == null) {
            return false;
        }
        if (otherValue == null) {
            return false;
        }

        if (value.getClass().getComponentType() != null) {
            return Objects.deepEquals(value, otherValue);
        }

        if (value instanceof Float || value instanceof Double) {
            return areEqualNumbers((Number) value, (Number) otherValue);
        }

        return value.equals(otherValue);
    }

    // TODO move elsewhere, handle div by zero
    public static boolean areEqualNumbers(Number a, Number b) {

        double da = a.doubleValue();
        double db = b.doubleValue();
        double delta = da - db;

        double pctDelta = delta / da;

        return pctDelta < 0.0001;
    }

    public static boolean hasDefaultValues(Rule rule) {

        Map<PropertyDescriptor<?>, Object> valuesByProperty = Configuration.filteredPropertiesOf(rule);
        for (Map.Entry<PropertyDescriptor<?>, Object> entry : valuesByProperty.entrySet()) {
            if (!isDefaultValue(entry)) {
                return false;
            }
        }
        return true;
    }

    public static Set<PropertyDescriptor<?>> modifiedPropertiesIn(Rule rule) {

        Set<PropertyDescriptor<?>> descs = new HashSet<>();

        for (Map.Entry<PropertyDescriptor<?>, Object> entry : Configuration.filteredPropertiesOf(rule).entrySet()) {
            if (isDefaultValue(entry)) {
                continue;
            }
            descs.add(entry.getKey());
        }

        return descs;
    }

    public static Set<Comparable<?>> uniqueItemsIn(Object item, RuleFieldAccessor getter) {

        if (item instanceof Rule) {
            Set<Comparable<?>> values = new HashSet<>(1);
            values.add(getter.valueFor((Rule) item));
            return values;
        }

        if (item instanceof RuleCollection) {
            return getter.uniqueValuesFor((RuleCollection) item);
        }

        return Collections.emptySet();
    }

    /**
     * Sometimes references reference references !
     * 
     * @param reference
     * @return
     */
    private static Class<Rule> rootImplementationClassOf(RuleReference reference) {

        Rule rule = reference.getRule();

        while (rule.getClass() == RuleReference.class) {
            rule = ((RuleReference) rule).getRule();
        }

        return (Class<Rule>) rule.getClass();
    }

    public static Class<Rule> implementationClassOf(Rule rule) {

        if (rule instanceof RuleReference) {
            return rootImplementationClassOf((RuleReference) rule);
        } else {
            return (Class<Rule>) rule.getClass();
        }
    }

    public static boolean allUseDefaultValues(RuleCollection collection) {
        if (collection.isEmpty()) {
            return false;
        }

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                return rule.getOverriddenPropertyDescriptors().isEmpty();
            }
        };

        return collection.rulesDo(visitor);
    }

    /**
     * Iterates through the currently selected rules and returns their common
     * priority setting or null if they differ.
     */
    public static RulePriority commonPriority(RuleCollection collection) {
        if (collection.isEmpty()) {
            return null;
        }

        final RulePriority[] prio = new RulePriority[1];

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                if (prio[0] == null) {
                    prio[0] = rule.getPriority();
                }
                if (prio[0] != rule.getPriority()) {
                    prio[0] = null;
                    return false;
                }
                return true;
            }
        };

        collection.rulesDo(visitor);

        return prio[0];
    }

    private static String format(Object item) {
        // TODO custom format per type
        return item == null ? "" : String.valueOf(item); 
    }

    public static String asString(Set<Comparable<?>> items) {

        Iterator<Comparable<?>> iter = items.iterator();
        if (items.size() == 1) {
            return format(iter.next());
        }

        StringBuilder sb = new StringBuilder(format(iter.next()));
        while (iter.hasNext()) {
            sb.append(", ").append(format(iter.next()));
        }
        return sb.toString();
    }

    public static Map<RulePriority, Float> fractionsByPriority(RuleCollection collection) {
        if (collection.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<RulePriority, Integer> priorityCounts = new HashMap<>(5);
        final int[] count = new int[1];

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                RulePriority priority = rule.getPriority();
                count[0] = count[0] + 1;
                Integer count = priorityCounts.get(priority);
                if (count == null) {
                    priorityCounts.put(priority, Integer.valueOf(1));
                } else {
                    priorityCounts.put(priority, Integer.valueOf(count + 1));
                }

                return true;
            }
        };

        collection.rulesDo(visitor);

        int total = count[0];

        Map<RulePriority, Float> priorityFractions = new HashMap<>();
        for (Map.Entry<RulePriority, Integer> entry : priorityCounts.entrySet()) {
            float fraction = (float) entry.getValue() / total;
            priorityFractions.put(entry.getKey(), fraction);
        }

        return priorityFractions;
    }

    /**
     * Iterates through the currently selected rules and returns their common
     * ruleset name or null if they differ.
     */
    public static String commonRuleset(RuleCollection collection) {
        if (collection.isEmpty()) {
            return null;
        }

        final Set<String> names = new HashSet<>(2);

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                names.add(rule.getRuleSetName().trim());
                return names.size() < 2;
            }
        };

        collection.rulesDo(visitor);

        return names.size() > 1 ? null : names.iterator().next();
    }

    public static Class<Rule> commonImplementationClass(RuleCollection collection) {
        if (collection.isEmpty()) {
            return null;
        }

        // TODO use array[1] approach like the others
        final Set<Class<Rule>> types = new HashSet<>(2);

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                types.add(implementationClassOf(rule));
                return types.size() < 2;
            }
        };

        collection.rulesDo(visitor);

        return types.size() > 1 ? null : types.iterator().next();
    }

    public static Comparable<?> commonAspect(RuleCollection collection, final RuleFieldAccessor accessor) {
        if (collection.isEmpty()) {
            return null;
        }

        final Comparable<?>[] aspect = new Comparable<?>[1];

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                if (aspect[0] == null) {
                    aspect[0] = accessor.valueFor(rule);
                    return true;
                }
                if (areEqual(aspect[0], accessor.valueFor(rule))) {
                    aspect[0] = null;
                    return false;
                }
                return true;
            }
        };

        collection.rulesDo(visitor);

        return aspect[0];
    }

    public static int countNonOccurrencesOf(RuleCollection collection, final RuleFieldAccessor accessor,
            final Object item) {

        if (collection.isEmpty()) {
            return 0;
        }

        final int[] count = new int[] { 0 };

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                Object value = accessor.valueFor(rule);
                if (!Objects.equals(value, item)) {
                    count[0] = count[0] + 1;
                }
                return true;
            }
        };

        collection.rulesDo(visitor);

        return count[0];
    }

    /**
     * Iterates through the currently selected rules and returns the set of
     * unique aspect values.
     */
    public static Set<Comparable<?>> uniqueAspects(RuleCollection collection, final RuleFieldAccessor accessor) {
        if (collection.isEmpty()) {
            return Collections.emptySet();
        }

        final Set<Comparable<?>> aspects = new HashSet<>();

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                aspects.add(accessor.valueFor(rule));
                return true;
            }
        };

        collection.rulesDo(visitor);

        return aspects;
    }

    public static Language commonLanguage(RuleCollection collection) {
        if (collection.isEmpty()) {
            return null;
        }

        final Language[] type = new Language[1];

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                if (type[0] == null) {
                    type[0] = rule.getLanguage();
                    return true;
                }
                if (!Objects.equals(type[0], rule.getLanguage())) {
                    type[0] = null;
                    return false;
                }
                return true;
            }
        };

        collection.rulesDo(visitor);

        return type[0];
    }

    public static LanguageVersion commonLanguageMinVersion(RuleCollection collection) {
        if (collection.isEmpty()) {
            return null;
        }

        final LanguageVersion[] version = new LanguageVersion[1];

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                if (version[0] == null) {
                    version[0] = rule.getMinimumLanguageVersion();
                    return true;
                }
                if (!Objects.equals(version[0], rule.getMinimumLanguageVersion())) {
                    version[0] = null;
                    return false;
                }
                return true;
            }
        };

        collection.rulesDo(visitor);

        return version[0];
    }

    public static LanguageVersion commonLanguageMaxVersion(RuleCollection collection) {
        if (collection.isEmpty()) {
            return null;
        }

        final LanguageVersion[] version = new LanguageVersion[1];

        RuleVisitor visitor = new RuleVisitor() {
            @Override
            public boolean accept(Rule rule) {
                if (version[0] == null) {
                    version[0] = rule.getMaximumLanguageVersion();
                    return true;
                }
                if (!Objects.equals(version[0], rule.getMaximumLanguageVersion())) {
                    version[0] = null;
                    return false;
                }
                return true;
            }
        };

        collection.rulesDo(visitor);

        return version[0];
    }
}
