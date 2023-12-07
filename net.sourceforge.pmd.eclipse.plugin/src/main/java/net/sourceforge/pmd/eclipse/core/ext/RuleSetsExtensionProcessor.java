/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.core.ext;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.eclipse.core.IRuleSetManager;
import net.sourceforge.pmd.eclipse.core.IRuleSetsExtension;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

/**
 * This class processes the AdditionalRuleSets extension point.
 *
 * @author Herlin
 *
 */

public class RuleSetsExtensionProcessor {
    private static final String EXTENTION_POINT = "net.sourceforge.pmd.eclipse.plugin.rulesets";
    private static final String CLASS_ATTRIBUTE = "class";
    private final IRuleSetManager ruleSetManager;

    public RuleSetsExtensionProcessor(IRuleSetManager theRuleSetManager) {
        this.ruleSetManager = theRuleSetManager;
    }

    /**
     * Process the extension point.
     */
    public void process() throws CoreException {
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        final IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENTION_POINT);
        for (IConfigurationElement element : extensionPoint.getConfigurationElements()) {
            processExecutableExtension(element);
        }
    }

    /**
     * Process an extension.
     * @param element the extension to process
     */
    private void processExecutableExtension(IConfigurationElement element) throws CoreException {
        final Object object = element.createExecutableExtension(CLASS_ATTRIBUTE);
        if (object instanceof IRuleSetsExtension) {
            final IRuleSetsExtension extension = (IRuleSetsExtension) object;

            Set<RuleSet> registeredRulesets = new LinkedHashSet<>(ruleSetManager.getRegisteredRuleSets());
            extension.registerRuleSets(registeredRulesets);
            ruleSetManager.getRegisteredRuleSets().clear();
            ruleSetManager.getRegisteredRuleSets().addAll(registeredRulesets);

            Set<RuleSet> defaultRegisteredRulesets = new LinkedHashSet<>(ruleSetManager.getDefaultRuleSets());
            extension.registerDefaultRuleSets(defaultRegisteredRulesets);
            ruleSetManager.getDefaultRuleSets().clear();
            ruleSetManager.getDefaultRuleSets().addAll(defaultRegisteredRulesets);
        } else {
            PMDPlugin.getDefault().log(IStatus.ERROR, "Extension " + element.getName() + " is not an instance of IRuleSetsExtension", null);
        }
    }

}
