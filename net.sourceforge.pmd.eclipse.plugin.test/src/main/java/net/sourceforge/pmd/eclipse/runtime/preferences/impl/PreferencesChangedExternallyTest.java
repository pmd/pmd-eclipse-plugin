/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.preferences.impl;

import java.io.File;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.eclipse.internal.ResourceUtil;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferencesManager;

public class PreferencesChangedExternallyTest {

    @Test
    public void changePreferenceExternally() throws Exception {
        IPreferencesManager preferencesManager = PMDPlugin.getDefault().getPreferencesManager();
        IPreferences preferences = preferencesManager.loadPreferences();
        try {
            Assert.assertTrue(preferences.isActive("AbstractClassWithoutAbstractMethod"));
    
            IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
            IPath prefsFile = workspaceRoot.getLocation().append(".metadata/.plugins/org.eclipse.core.runtime/.settings/net.sourceforge.pmd.eclipse.plugin.prefs");
            File prefsFileReal = prefsFile.toFile();
            ResourceUtil.copyResource(this, "pmd.prefs", prefsFileReal);
    
            IPreferences preferences2 = preferencesManager.loadPreferences();
            Assert.assertFalse(preferences2.isActive("AbstractClassWithoutAbstractMethod"));
            Assert.assertTrue(preferences2.isActive("DoNotCallSystemExit"));
            Assert.assertNotSame(preferences, preferences2);
        } finally {
            preferencesManager.storePreferences(preferences);
        }
    }
}
