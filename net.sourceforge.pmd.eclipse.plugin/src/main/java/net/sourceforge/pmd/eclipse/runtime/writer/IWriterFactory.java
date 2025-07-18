/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.writer;

/**
 * Interface of a factory for the writer package objects
 * 
 * @author Philippe Herlin
 *
 */
public interface IWriterFactory {

    /**
     * @return a ruleset writer
     */
    IRuleSetWriter getRuleSetWriter();

    /**
     * @return an AST writer
     */
    IAstWriter getAstWriter();
}
