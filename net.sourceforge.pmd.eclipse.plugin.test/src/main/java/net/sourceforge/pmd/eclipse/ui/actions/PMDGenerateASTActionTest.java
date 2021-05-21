/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.actions;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.EclipseUtils;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;

public class PMDGenerateASTActionTest {
    private IProject testProject;
    private IFile testFile;

    @Before
    public void setUp() throws Exception {
        // 1. Create a Java project
        this.testProject = EclipseUtils.createJavaProject("PMDGenerateASTActionTest");
        Assert.assertTrue("A test project cannot be created; the tests cannot be performed.",
                this.testProject != null && this.testProject.exists() && this.testProject.isAccessible());

        // 2. Create a test source file inside that project
        this.testFile = EclipseUtils.createTestSourceFile(this.testProject);
        final InputStream is = EclipseUtils.getResourceStream(this.testProject, "/src/Test.java");
        Assert.assertNotNull("Cannot find the test source file", is);
        is.close();

        // 3. Enable PMD for the test project
        IProjectProperties properties = PMDPlugin.getDefault().getPropertiesManager()
                .loadProjectProperties(testProject);
        properties.setPmdEnabled(true);
    }

    @After
    public void tearDown() throws Exception {
        try {
            if (this.testProject != null) {
                if (this.testProject.exists() && this.testProject.isAccessible()) {
                    EclipseUtils.removePMDNature(this.testProject);
                    this.testProject.refreshLocal(IResource.DEPTH_INFINITE, null);
                    this.testProject.delete(true, true, null);
                    this.testProject = null;
                }
            }
        } catch (final Exception e) {
            System.out.println("Exception " + e.getClass().getName() + " when tearing down. Ignored.");
        }
    }

    @Test
    public void runGenerateASTAction() throws CoreException, InterruptedException, IOException {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                ISelection selection = new StructuredSelection(testFile);
                PMDGenerateASTAction action = new PMDGenerateASTAction();
                action.selectionChanged(null, selection);
                action.run((IAction) null);
            }
        });

        String astFilename = FilenameUtils.getBaseName(testFile.getName()) + ".ast";
        IResource astFile;
        long start = System.currentTimeMillis();
        do {
            Thread.sleep(500);
            astFile = testFile.getParent().findMember(astFilename);
        } while (astFile == null || (System.currentTimeMillis() - start) > 60_000);

        Assert.assertNotNull("No AST file has been generated", astFile);
        IFile adapter = (IFile) astFile.getAdapter(IFile.class);
        String content = IOUtils.toString(adapter.getContents(), adapter.getCharset());
        Assert.assertTrue(content.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        Assert.assertTrue(content.contains("<CompilationUnit"));
    }
}
