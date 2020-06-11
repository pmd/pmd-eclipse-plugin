/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.preferences.impl;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.eclipse.internal.ResourceUtil;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferencesManager;


public class ChangeGlobalRuleSetExternallyTest {

    @Test
    public void changeRuleSetExternally() throws Exception {
        IPreferencesManager preferencesManager = PMDPlugin.getDefault().getPreferencesManager();
        RuleSet ruleSet = preferencesManager.getRuleSet();
        int numberOfRules = ruleSet.size();
        Assert.assertTrue(numberOfRules > 1);
        
        // Now change the ruleset on disk
        IPath ruleSetFile = PMDPlugin.getDefault().getStateLocation().append("/ruleset.xml");
        File ruleSetRealFile = ruleSetFile.toFile();
        ResourceUtil.copyResource(this, "test-ruleset.xml", ruleSetRealFile);

        RuleSet ruleSet2 = preferencesManager.getRuleSet();
        Assert.assertEquals(1, ruleSet2.size());
    }

    @After
    public void cleanup() {
        IPreferencesManager preferencesManager = PMDPlugin.getDefault().getPreferencesManager();
        preferencesManager.setRuleSet(preferencesManager.getDefaultRuleSet());
    }
}
