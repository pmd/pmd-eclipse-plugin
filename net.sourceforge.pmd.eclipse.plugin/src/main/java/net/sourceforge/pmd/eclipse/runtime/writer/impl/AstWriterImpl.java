/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.writer.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import net.sourceforge.pmd.eclipse.runtime.writer.IAstWriter;
import net.sourceforge.pmd.eclipse.runtime.writer.WriterException;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.util.treeexport.XmlTreeRenderer;

/**
 * Implements a default AST Writer
 *
 * @author Philippe Herlin
 *
 */
class AstWriterImpl implements IAstWriter {
    @Override
    public void write(OutputStream outputStream, ASTCompilationUnit compilationUnit) throws WriterException {
        try {
            XmlTreeRenderer renderer = new XmlTreeRenderer();
            renderer.renderSubtree(compilationUnit, new PrintStream(outputStream, true, StandardCharsets.UTF_8.name()));
        } catch (IOException e) {
            throw new WriterException(e);
        }
    }
}
