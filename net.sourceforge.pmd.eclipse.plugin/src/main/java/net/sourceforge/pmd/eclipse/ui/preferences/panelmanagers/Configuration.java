/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.panelmanagers;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.lang.rule.XPathRule;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 *
 * @author Brian Remedios
 */
public final class Configuration {
    private Configuration() { }

    // properties that should not be shown in the PerRuleProperty page
    public static final PropertyDescriptor<?>[] EXCLUDED_RULE_PROPERTIES = new PropertyDescriptor<?>[] {
        Rule.VIOLATION_SUPPRESS_REGEX_DESCRIPTOR, Rule.VIOLATION_SUPPRESS_XPATH_DESCRIPTOR, XPathRule.XPATH_DESCRIPTOR,
        XPathRule.VERSION_DESCRIPTOR, };

    public static Map<PropertyDescriptor<?>, Object> filteredPropertiesOf(PropertySource source) {
        Map<PropertyDescriptor<?>, Object> valuesByProp = new HashMap<>(source.getPropertiesByPropertyDescriptor());

        for (PropertyDescriptor<?> excludedRuleProperty : EXCLUDED_RULE_PROPERTIES) {
            valuesByProp.remove(excludedRuleProperty);
        }

        return valuesByProp;
    }
}
