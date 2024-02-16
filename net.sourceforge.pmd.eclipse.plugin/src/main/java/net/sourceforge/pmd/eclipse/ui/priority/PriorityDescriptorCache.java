/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.priority;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.plugin.UISettings;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferences;
import net.sourceforge.pmd.eclipse.runtime.preferences.IPreferencesManager;
import net.sourceforge.pmd.lang.rule.RulePriority;

/**
 * 
 * @author Brian Remedios
 */
public final class PriorityDescriptorCache {
    private final ConcurrentMap<RulePriority, PriorityDescriptor> uiDescriptorsByPriority = new ConcurrentHashMap<>(RulePriority.values().length);

    public static final PriorityDescriptorCache INSTANCE = new PriorityDescriptorCache();

    private PriorityDescriptorCache() {
        loadFromPreferences();
    }

    private IPreferencesManager preferencesManager() {
        return PMDPlugin.getDefault().getPreferencesManager();
    }

    public void loadFromPreferences() {
        IPreferences preferences = preferencesManager().loadPreferences();
        for (RulePriority rp : UISettings.currentPriorities(true)) {
            // note: the priority descriptors are cloned here, so that any changes to them
            // do not automatically get stored. Changes might occur while configuring the
            // preferences, but the user might cancel.
            uiDescriptorsByPriority.put(rp, preferences.getPriorityDescriptor(rp).clone());
        }
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
        // remove old images so that the images are recreated with the changed settings
        dispose();
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
}
