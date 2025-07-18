/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.actions;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sourceforge.pmd.lang.rule.Rule;
import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.lang.rule.RuleSetLoader;

/**
 * 
 * @author Brian Remedios
 */
public final class RuleSetUtil {

    private RuleSetUtil() {
    }

    public static RuleSet newCopyOf(RuleSet original) {
        return RuleSet.copy(original);
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
        return RuleSet.create(ruleSet.getName(), ruleSet.getDescription(),
                ruleSet.getFileName(),
                ruleSet.getFileExclusions(),
                ruleSet.getFileInclusions(),
                wantedRules);
    }

    /**
     * Removes the rule with the same name from the ruleset.
     * 
     * @param ruleSet
     * @param removedRule
     * @return
     */
    public static RuleSet removeRule(RuleSet ruleSet, Rule removedRule) {
        List<Rule> wantedRules = new ArrayList<>(ruleSet.getRules());
        wantedRules.remove(removedRule);
        return retainOnly(ruleSet, wantedRules);
    }

    public static RuleSet newSingle(Rule rule) {
        return RuleSet.forSingleRule(rule);
    }

    public static RuleSet newEmpty(String name, String description) {
        Set<Pattern> emptySet = Collections.emptySet();
        Set<Rule> emptyRules = Collections.emptySet();
        return RuleSet.create(name, description, null, emptySet, emptySet, emptyRules);
    }

    public static RuleSet addRuleSetByReference(RuleSet ruleSet, RuleSet sourceRuleSet) {
        StringWriter ruleSetXml = new StringWriter();
        try {
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(ruleSetXml);
            writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
            writer.writeStartElement("ruleset");
            writer.writeAttribute("name", ruleSet.getName());
            writer.writeStartElement("description");
            writer.writeCharacters(ruleSet.getDescription());
            writer.writeEndElement();

            for (Rule rule : sourceRuleSet.getRules()) {
                writer.writeStartElement("rule");
                writer.writeAttribute("ref", sourceRuleSet.getFileName() + "/" + rule.getName());
                writer.writeEndElement();
            }

            writer.writeEndElement();
            writer.flush();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        RuleSetLoader ruleSetLoader = new RuleSetLoader();
        RuleSet withReferences = ruleSetLoader.loadFromString("temporary-ruleset.xml", ruleSetXml.toString());
        List<Rule> allRulesList = new ArrayList<>(ruleSet.getRules());
        allRulesList.addAll(withReferences.getRules());

        return RuleSet.create(ruleSet.getName(), ruleSet.getDescription(),
                ruleSet.getFileName(),
                ruleSet.getFileExclusions(),
                ruleSet.getFileInclusions(),
                allRulesList);
    }

    public static RuleSet addRules(RuleSet ruleSet, Collection<Rule> newRules) {
        Collection<Rule> allRules = new ArrayList<>();
        allRules.addAll(ruleSet.getRules());
        allRules.addAll(newRules);
        return RuleSet.create(ruleSet.getName(), ruleSet.getDescription(),
                ruleSet.getFileName(),
                ruleSet.getFileExclusions(),
                ruleSet.getFileInclusions(),
                allRules);
    }

    public static RuleSet addRule(RuleSet ruleSet, Rule newRule) {
        return addRules(ruleSet, Collections.singleton(newRule));
    }

    public static RuleSet setNameDescription(RuleSet ruleSet, String name, String description) {
        return RuleSet.create(name, description, ruleSet.getFileName(),
                ruleSet.getFileExclusions(),
                ruleSet.getFileInclusions(),
                ruleSet.getRules());
    }

    public static RuleSet setFileName(RuleSet ruleSet, String fileName) {
        return RuleSet.create(ruleSet.getName(), ruleSet.getDescription(),
                fileName,
                ruleSet.getFileExclusions(),
                ruleSet.getFileInclusions(),
                ruleSet.getRules());
    }

    public static RuleSet clearRules(RuleSet ruleSet) {
        Set<Rule> emptySet = Collections.emptySet();
        return RuleSet.create(ruleSet.getName(), ruleSet.getDescription(),
                ruleSet.getFileName(),
                ruleSet.getFileExclusions(),
                ruleSet.getFileInclusions(),
                emptySet);
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
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
