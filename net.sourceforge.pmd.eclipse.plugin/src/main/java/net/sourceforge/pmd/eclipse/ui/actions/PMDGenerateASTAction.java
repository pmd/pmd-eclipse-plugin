/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.writer.IAstWriter;
import net.sourceforge.pmd.eclipse.runtime.writer.WriterException;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageProcessor;
import net.sourceforge.pmd.lang.LanguageProcessorRegistry;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.ast.ParseException;
import net.sourceforge.pmd.lang.ast.Parser;
import net.sourceforge.pmd.lang.ast.Parser.ParserTask;
import net.sourceforge.pmd.lang.ast.SemanticErrorReporter;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.document.TextDocument;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.internal.JavaLanguageProperties;

/**
 * Process PMDGenerateAST action menu. Generate a AST from the selected file.
 *
 * @author Philippe Herlin
 *
 */
public class PMDGenerateASTAction extends AbstractUIAction implements IRunnableWithProgress {

    private static final Logger LOG = LoggerFactory.getLogger(PMDGenerateASTAction.class);

    @Override
    public void run(IAction action) {
        LOG.info("Generation AST action requested");

        ISelection sel = targetSelection();
        if (sel instanceof IStructuredSelection) {
            ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
            try {
                dialog.run(false, false, this);
            } catch (InvocationTargetException e) {
                showErrorById(StringKeys.ERROR_INVOCATIONTARGET_EXCEPTION, e);
            } catch (InterruptedException e) {
                showErrorById(StringKeys.ERROR_INTERRUPTED_EXCEPTION, e);
            }
        }
    }

    /**
     * Generate a AST for a file
     * 
     * @param file
     *            a file
     */
    private void generateAST(IFile file) {
        LOG.info("Generating AST for file " + file.getName());
        Language javaLanguage = LanguageRegistry.PMD.getLanguageById(JavaLanguageModule.TERSE_NAME);
        try (Reader reader = new InputStreamReader(file.getContents(), file.getCharset());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                LanguageProcessor javaProcessor = javaLanguage.createProcessor(new JavaLanguageProperties());
                TextDocument textDocument = TextDocument.create(TextFile.forReader(reader, FileId.fromPathLikeString(file.getName()), javaLanguage.getDefaultVersion()));) {

            Parser parser = javaProcessor.services().getParser();
            ParserTask parserTask = new ParserTask(textDocument, SemanticErrorReporter.noop(), LanguageProcessorRegistry.singleton(javaProcessor));
            ASTCompilationUnit compilationUnit = (ASTCompilationUnit) parser.parse(parserTask);
            IAstWriter astWriter = PMDPlugin.getDefault().getAstWriter();
            astWriter.write(byteArrayOutputStream, compilationUnit);
            byteArrayOutputStream.flush();

            IFile astFile = createASTFile(file);

            if (astFile != null) {
                if (astFile.exists()) {
                    astFile.delete(false, null);
                }
                try (ByteArrayInputStream astInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
                    astFile.create(astInputStream, false, null);
                }
            }

        } catch (CoreException e) {
            showErrorById(StringKeys.ERROR_CORE_EXCEPTION, e);
        } catch (ParseException | WriterException e) {
            showErrorById(StringKeys.ERROR_PMD_EXCEPTION, e);
        } catch (IOException e) {
            showErrorById(StringKeys.ERROR_IO_EXCEPTION, e);
        } catch (Exception e) {
            showErrorById(StringKeys.ERROR_PMD_EXCEPTION, e);
        }
    }

    private static IFile createASTFile(IFile file) {

        String astName = astNameFor(file);

        IFile astFile = null;
        IContainer parent = file.getParent();
        if (parent instanceof IFolder) {
            astFile = ((IFolder) parent).getFile(astName);
        } else if (parent instanceof IProject) {
            astFile = ((IProject) parent).getFile(astName);
        }
        return astFile;
    }

    private static String astNameFor(IFile file) {

        String name = file.getName();
        int dotPosition = name.indexOf('.');
        return name.substring(0, dotPosition) + ".ast";
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        IStructuredSelection structuredSelection = (IStructuredSelection) targetSelection();
        monitor.beginTask("", structuredSelection.size());
        for (Iterator<?> i = structuredSelection.iterator(); i.hasNext();) {
            Object element = i.next();
            if (element instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) element;
                IResource resource = (IResource) adaptable.getAdapter(IResource.class);
                if (resource != null) {
                    monitor.subTask(resource.getName());
                    generateAST((IFile) resource);
                    monitor.worked(1);
                } else {
                    LOG.warn("The selected object cannot adapt to a resource");
                    LOG.debug("   -> selected object : " + element);
                }
            } else {
                LOG.warn("The selected object is not adaptable");
                LOG.debug("   -> selected object : " + element);
            }
        }
    }

}
