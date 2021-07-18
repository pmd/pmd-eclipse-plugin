/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.writer;

import java.io.OutputStream;

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;

/**
 * Interface of an AST Writer. An IAstWriter is an object used to "serialize" an
 * AST.
 * 
 * @author Philippe Herlin
 * 
 */
public interface IAstWriter {

    /**
     * Serialize an AST into an output stream.
     * 
     * @param outputStream
     *            the target output
     * @param compilationUnit
     *            the compilation unit to serialize
     * @throws WriterException
     */
    void write(OutputStream outputStream, ASTCompilationUnit compilationUnit) throws WriterException;

}
