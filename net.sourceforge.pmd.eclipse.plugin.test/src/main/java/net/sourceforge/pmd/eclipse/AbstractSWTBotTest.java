/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class AbstractSWTBotTest {
    protected static SWTWorkbenchBot bot;

    @BeforeClass
    public static void initBot() throws InterruptedException {
        bot = new SWTWorkbenchBot();
        for (SWTBotView view : bot.views()) {
            if ("Welcome".equals(view.getTitle())) {
                view.close();
            }
        }
        openJavaPerspective();
    }

    @AfterClass
    public static void afterClass() {
        try {
            bot.resetWorkbench();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void openJavaPerspective() throws InterruptedException {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    IWorkbench workbench = PlatformUI.getWorkbench();
                    IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
                    workbench.showPerspective(JavaUI.ID_PERSPECTIVE, activeWorkbenchWindow);
                    IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
                    activePage.showView(ProjectExplorer.VIEW_ID);
                } catch (WorkbenchException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
