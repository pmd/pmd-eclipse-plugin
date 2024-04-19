/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.ui.preferences.br;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;
import static org.eclipse.swtbot.swt.finder.waits.Conditions.waitForWidget;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swtbot.swt.finder.waits.WaitForObjectCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.AbstractSWTBotTest;

public class PMDPreferencePage2Test extends AbstractSWTBotTest {
    private List<IStatus> errors;
    private ILogListener logListener;

    @Before
    public void setup() {
        errors = new ArrayList<>();
        logListener = new ILogListener() {
            @Override
            public void logging(IStatus status, String plugin) {
                if (status.getSeverity() != IStatus.ERROR) {
                    return;
                }

                String statusText = status.toString().toLowerCase(Locale.ROOT);
                if (statusText.contains("pmd") || statusText.contains("unhandled event loop exception")) {
                    errors.add(status);
                }
            }
        };
        Platform.getLog(PlatformUI.class).addLogListener(logListener);
    }

    @After
    public void cleanup() {
        Platform.getLog(PlatformUI.class).removeLogListener(logListener);
    }

    /**
     * Simple smoke test to open the rule configuration preference pages.
     * @see PMDPreferencePage2
     */
    @Test
    public void openPMDRuleConfiguration() {
        openPreferences();
        SWTBotShell preferencesDialog = bot.shell("Preferences");
        SWTBotTreeItem pmdItem = preferencesDialog.bot().tree(0).getTreeItem("PMD");
        pmdItem.click();
        pmdItem.expand();
        pmdItem.getNode("Rule Configuration").click();


        Matcher<Button> matcher = allOf(widgetOfType(Button.class),
                withMnemonic("Use global rule management"),
                withStyle(SWT.CHECK, "SWT.CHECK"));
        WaitForObjectCondition<Button> waitForWidget = waitForWidget(matcher);
        preferencesDialog.bot().waitUntil(waitForWidget, TimeUnit.SECONDS.toMillis(15));
        SWTBotCheckBox globalRuleManagementCheckbox = new SWTBotCheckBox(waitForWidget.get(0), matcher);
        globalRuleManagementCheckbox.click();

        SWTBotTree ruleTable = preferencesDialog.bot().tree(1);
        int rowCount = ruleTable.rowCount();
        assertTrue(rowCount > 0);

        // select first rule
        ruleTable.getAllItems()[0].click();

        preferencesDialog.bot().button("Cancel").click();

        if (!errors.isEmpty()) {
            fail("There are " + errors.size() + " errors:\n"
                    + errors.stream().map(IStatus::getMessage).collect(Collectors.joining("\n")));
        }
    }

    private void openPreferences() {
        // due to macosx, where the Preferences is not part of the Window menu but the application
        // menu, we programmatically open the preferences dialog and not try to find it via the menu.
        bot.getDisplay().execute(() -> {
            PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(bot.shell().widget, null, null, errors);
            dialog.open();
        });
    }
}
