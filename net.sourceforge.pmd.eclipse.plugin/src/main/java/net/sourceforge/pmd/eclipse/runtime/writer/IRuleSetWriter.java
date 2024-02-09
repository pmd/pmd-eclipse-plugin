/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.writer;

import java.io.OutputStream;

import net.sourceforge.pmd.lang.rule.RuleSet;

/**
 * Interface for a rule set writer. A rule set writer is an object used to
 * "serialize" a rule set.
 * 
 * @author Philippe Herlin
 *
 */
public interface IRuleSetWriter {

    /**
     * Write a ruleset as an XML stream.
     * 
     * @param outputStream
     *            the output target
     * @param ruleSet
     *            the ruleset to serialize
     */
    void write(OutputStream outputStream, RuleSet ruleSet) throws WriterException;
}
