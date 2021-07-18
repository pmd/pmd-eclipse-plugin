/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences;

import net.sourceforge.pmd.eclipse.util.Util;

/**
 * This class implements a content provider for the rule properties table of the
 * PMD Preference page
 *
 * @author Philippe Herlin
 * @deprecated
 */
@Deprecated
public class RulePropertiesContentProvider extends AbstractStructuredContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {

        // if (inputElement instanceof Rule) {
        // Rule rule = (Rule) inputElement;
        // Enumeration<String> keys = rule.getProperties().keys();
        // List<RuleProperty> propertyList = new ArrayList<RuleProperty>();
        // while (keys.hasMoreElements()) {
        // propertyList.add(new RuleProperty(rule, keys.nextElement()));
        // }
        // return propertyList.toArray();
        // }

        return Util.EMPTY_ARRAY;
    }
}
