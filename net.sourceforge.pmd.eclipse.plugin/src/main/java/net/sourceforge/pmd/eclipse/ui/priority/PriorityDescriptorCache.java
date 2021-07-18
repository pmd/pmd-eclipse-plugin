/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.priority;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.plugin.UISettings;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferencesManager;

/**
 * 
 * @author Brian Remedios
 */
public final class PriorityDescriptorCache {
    private Map<RulePriority, PriorityDescriptor> uiDescriptorsByPriority;

    public static final PriorityDescriptorCache INSTANCE = new PriorityDescriptorCache();

    private PriorityDescriptorCache() {
        uiDescriptorsByPriority = new HashMap<>(RulePriority.values().length);
        loadFromPreferences();
    }

    private IPreferencesManager preferencesManager() {
        return PMDPlugin.getDefault().getPreferencesManager();
    }

    @Deprecated
    public void dumpTo(PrintStream out) {
        for (Map.Entry<RulePriority, PriorityDescriptor> entry : uiDescriptorsByPriority.entrySet()) {
            out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    public void loadFromPreferences() {
        IPreferences preferences = preferencesManager().loadPreferences();
        for (RulePriority rp : UISettings.currentPriorities(true)) {
            // note: the priority descriptors are cloned here, so that any changes to them
            // do not automatically get stored. Changes might occur while configuring the
            // preferences, but the user might cancel.
            uiDescriptorsByPriority.put(rp, preferences.getPriorityDescriptor(rp).clone());
        }
        refreshImages();
    }

    public void storeInPreferences() {
        IPreferencesManager mgr = preferencesManager();
        IPreferences prefs = mgr.loadPreferences();

        for (Map.Entry<RulePriority, PriorityDescriptor> entry : uiDescriptorsByPriority.entrySet()) {
            // note: the priority descriptors are cloned here, so that any changes to them
            // do not automatically get stored. Changes might occur while configuring the
            // preferences, but the user might cancel.
            prefs.setPriorityDescriptor(entry.getKey(), entry.getValue().clone());
        }
        prefs.sync();
        // recreate images with the changed settings
        refreshImages();
    }

    public PriorityDescriptor descriptorFor(RulePriority priority) {
        return uiDescriptorsByPriority.get(priority);
    }

    public boolean hasChanges() {
        IPreferences currentPreferences = preferencesManager().loadPreferences();

        for (RulePriority rp : UISettings.currentPriorities(true)) {
            PriorityDescriptor newOne = uiDescriptorsByPriority.get(rp);
            PriorityDescriptor currentOne = currentPreferences.getPriorityDescriptor(rp);
            if (newOne.equals(currentOne)) {
                continue;
            } else {
                return true;
            }
        }
        return false;
    }

    public void dispose() {
        for (PriorityDescriptor pd : uiDescriptorsByPriority.values()) {
            pd.dispose();
        }
    }

    private void refreshImages() {
        for (PriorityDescriptor pd : uiDescriptorsByPriority.values()) {
            pd.refreshImages();
        }
    }
}
