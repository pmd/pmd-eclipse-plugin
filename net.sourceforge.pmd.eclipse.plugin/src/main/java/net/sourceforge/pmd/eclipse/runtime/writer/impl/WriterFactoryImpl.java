/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.writer.impl;

import net.sourceforge.pmd.eclipse.runtime.writer.IAstWriter;
import net.sourceforge.pmd.eclipse.runtime.writer.IRuleSetWriter;
import net.sourceforge.pmd.eclipse.runtime.writer.IWriterFactory;

/**
 * The writer factory produces writers such as the one for the ruleset file.
 * This class is the abstract base class for writer factories.
 * 
 * @author Philippe Herlin
 *
 */
public class WriterFactoryImpl implements IWriterFactory {

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.writer.IWriterFactory#getRuleSetWriter()
     */
    public IRuleSetWriter getRuleSetWriter() {
        return new RuleSetWriterImpl();
    }

    /**
     * @see net.sourceforge.pmd.eclipse.runtime.writer.IWriterFactory#getAstWriter()
     */
    public IAstWriter getAstWriter() {
        return new AstWriterImpl();
    }
}
