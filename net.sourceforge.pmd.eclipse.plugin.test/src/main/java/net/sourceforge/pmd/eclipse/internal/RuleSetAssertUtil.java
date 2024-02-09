/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Pattern;

import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.properties.PropertyDescriptor;

public final class RuleSetAssertUtil {
    private static final Comparator<PropertyDescriptor<?>> BY_NAME = (a, b) -> a.name().compareTo(b.name());

    private RuleSetAssertUtil() {
        // utility
    }

    public static void assertEqualsRules(RuleSet expected, RuleSet actual) {
        assertEquals(expected.getRules().size(), actual.getRules().size());

        Iterator<Rule> expectedIterator = expected.getRules().iterator();
        Iterator<Rule> actualIterator = actual.getRules().iterator();

        for (int i = 0; i < actual.getRules().size(); i++) {
            Rule expectedRule = expectedIterator.next();
            Rule actualRule = actualIterator.next();
            assertSame("Wrong rule class", expectedRule.getClass(), actualRule.getClass());
            assertEquals("Wrong rule name", expectedRule.getName(), actualRule.getName());
            assertEquals("Wrong priority", expectedRule.getPriority(), actualRule.getPriority());

            // sort properties by name
            Map<PropertyDescriptor<?>, Object> expectedProperties = new TreeMap<>(BY_NAME);
            expectedProperties.putAll(expectedRule.getPropertiesByPropertyDescriptor());
            Map<PropertyDescriptor<?>, Object> actualProperties = new TreeMap<>(BY_NAME);
            actualProperties.putAll(actualRule.getPropertiesByPropertyDescriptor());
            assertEquals(expectedProperties.size(), actualProperties.size());

            Iterator<Entry<PropertyDescriptor<?>, Object>> expectedPropertiesIterator = expectedProperties.entrySet().iterator();
            Iterator<Entry<PropertyDescriptor<?>, Object>> actualPropertiesIterator = actualProperties.entrySet().iterator();
            for (int j = 0; j < expectedProperties.size(); j++) {
                Entry<PropertyDescriptor<?>, Object> expectedProperty = expectedPropertiesIterator.next();
                Entry<PropertyDescriptor<?>, Object> actualProperty = actualPropertiesIterator.next();
                assertEquals(expectedProperty.getKey(), actualProperty.getKey());
                // java.util.regex.Pattern can't be compared using equals...
                if (expectedProperty.getValue() instanceof Pattern) {
                    Pattern expectedPattern = (Pattern) expectedProperty.getValue();
                    Pattern actualPattern = (Pattern) actualProperty.getValue();
                    assertEquals(expectedPattern.pattern(), actualPattern.pattern());
                } else {
                    assertEquals(expectedProperty.getValue(), actualProperty.getValue());
                }
            }
        }
    }

    public static void compareTwoRuleSets(RuleSet ruleSet1, RuleSet ruleSet2) {
        if (!ruleSet1.getRules().equals(ruleSet2.getRules())) {
            System.out.println("###################################################");
            System.out.println("RuleSet1: " + ruleSet1 + " (count " + ruleSet1.size() + ") RuleSet2: " + ruleSet2 + " (count " + ruleSet2.size() + ")");
            Iterator<Rule> it1 = ruleSet1.getRules().iterator();
            Iterator<Rule> it2 = ruleSet2.getRules().iterator();
            for (int i = 0; i < ruleSet2.getRules().size(); i++) {
                Rule pluginRule = it1.next();
                Rule projectRule = it2.next();

                if (!Objects.equals(pluginRule, projectRule)) {
                    System.out.println("i=" + i + ": pluginRule=" + pluginRule + " projectRule=" + projectRule);
                    System.out.println("plugin: " + pluginRule.getName() + " (" + pluginRule.getLanguage() + ")");
                    System.out.println("project: " + projectRule.getName() + " (" + projectRule.getLanguage() + ")");
                }
            }
            System.out.println("###################################################");
        }
    }
}
