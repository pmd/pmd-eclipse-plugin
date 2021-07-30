/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.properties.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class is a simple data bean to help serialize project properties. Is
 * used by the ProjectPropertiesTO to handle project selected rules. This class
 * holds single rule information.
 * 
 * @author Philippe Herlin
 * 
 */
@XmlType(propOrder = { "name", "ruleSetName" })
public class RuleSpecTO {
    private String name;
    private String ruleSetName;

    public RuleSpecTO() {
        super();
    }

    /**
     * Constructor with fields.
     * 
     * @param name
     *            a rule name
     * @param ruleSetName
     *            the name of the ruleset where the rule is defined
     */
    public RuleSpecTO(final String name, final String ruleSetName) {
        super();
        this.name = name;
        this.ruleSetName = ruleSetName;
    }

    /**
     * @return name a rule name
     */
    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    /**
     * Set the rule name
     * 
     * @param name
     *            the rule name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return ruleSetName the name of ruleset the rule come from
     */
    @XmlElement(name = "ruleset")
    public String getRuleSetName() {
        return ruleSetName;
    }

    /**
     * Set the ruleSet name the rule come from
     * 
     * @param ruleSetName
     *            a ruleSet name
     */
    public void setRuleSetName(final String ruleSetName) {
        this.ruleSetName = ruleSetName;
    }
}
