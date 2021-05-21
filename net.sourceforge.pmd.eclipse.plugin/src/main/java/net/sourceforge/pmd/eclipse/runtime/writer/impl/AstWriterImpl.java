/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.writer.impl;

import java.io.OutputStream;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import net.sourceforge.pmd.eclipse.runtime.writer.IAstWriter;
import net.sourceforge.pmd.eclipse.runtime.writer.WriterException;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;

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
            Document doc = compilationUnit.getAsDocument();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
        } catch (DOMException | FactoryConfigurationError | TransformerException e) {
            throw new WriterException(e);
        }
    }
}
