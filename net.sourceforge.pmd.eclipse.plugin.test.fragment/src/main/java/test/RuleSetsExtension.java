/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package test;

import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.event.Level;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetLoadException;
import net.sourceforge.pmd.RuleSetLoader;
import net.sourceforge.pmd.eclipse.core.IRuleSetsExtension;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.util.log.MessageReporter;

/**
 * Sample of an RuleSets extension.
 * This will automatically registers our fragment rulesets into the core plugin.
 *
 * @author Herlin
 *
 */

public class RuleSetsExtension implements IRuleSetsExtension {
    private RuleSet ruleSet1;
    private RuleSet ruleSet2;

    /**
     * Replace the core plugin fragment with our own rulesets
     * @see net.sourceforge.pmd.eclipse.core.IRuleSetsExtension#registerRuleSets(java.util.Set)
     */
    @Override
    public void registerRuleSets(Set<RuleSet> registeredRuleSets) {
        try {
            RuleSet ruleSet1 = getRuleSet1();
            RuleSet ruleSet2 = getRuleSet2();

            // registeredRuleSets.clear(); // to remove all rulesets already registered
            registeredRuleSets.add(ruleSet1);
            registeredRuleSets.add(ruleSet2);
        } catch (RuleSetLoadException e) {
            PMDPlugin.getDefault().log(IStatus.ERROR, "Unable to load rulesets", e);
        }
    }

    /**
     * Replace the default rule sets. These rule sets are the one loaded if no rule sets has been configured yet
     * (for instance when creating a new workspace)
     * @see net.sourceforge.pmd.eclipse.core.IRuleSetsExtension#registerDefaultRuleSets(java.util.Set)
     */
    @Override
    public void registerDefaultRuleSets(Set<RuleSet> defaultRuleSets) {
        try {
            RuleSet ruleSet1 = getRuleSet1();
            RuleSet ruleSet2 = getRuleSet2();

            // registeredRuleSets.clear(); // to remove all rulesets already registered
            defaultRuleSets.add(ruleSet1);
            defaultRuleSets.add(ruleSet2);
        } catch (RuleSetLoadException e) {
            PMDPlugin.getDefault().log(IStatus.ERROR, "Unable to load rulesets", e);
        }
    }

    /**
     * Load the 1st ruleset
     * @return the 1st ruleset
     * @throws RuleSetLoadException
     */
    private RuleSet getRuleSet1() throws RuleSetLoadException {
        if (this.ruleSet1 == null) {
            PMDConfiguration configuration = new PMDConfiguration();
            configuration.setReporter(new RulesetLoaderMessageReporter());
            RuleSetLoader ruleSetLoader = RuleSetLoader.fromPmdConfig(configuration);
            this.ruleSet1 = ruleSetLoader.loadFromResource("rulesets/extra1.xml");
        }

        return this.ruleSet1;
    }

    /**
     * Load the 2nd ruleset
     * @return the 2nd ruleset
     * @throws RuleSetLoadException
     */
    private RuleSet getRuleSet2() throws RuleSetLoadException {
        if (this.ruleSet2 == null) {
            PMDConfiguration configuration = new PMDConfiguration();
            configuration.setReporter(new RulesetLoaderMessageReporter());
            RuleSetLoader ruleSetLoader = RuleSetLoader.fromPmdConfig(configuration);
            this.ruleSet2 = ruleSetLoader.loadFromResource("rulesets/extra2.xml");
        }

        return this.ruleSet2;
    }

    private static final class RulesetLoaderMessageReporter implements MessageReporter {
        private int errors = 0;

        @Override
        public int numErrors() {
            return errors;
        }

        @Override
        public void logEx(Level level, @Nullable String message, Object[] formatArgs, @Nullable Throwable error) {
            if (Level.ERROR == level) {
                errors++;
                PMDPlugin.getDefault().logError(Status.error(message));
            } else if (Level.WARN == level) {
                PMDPlugin.getDefault().logWarn(message);
            } else {
                PMDPlugin.getDefault().logInformation(message);
            }
        }

        @Override
        public boolean isLoggable(Level level) {
            return true;
        }
    }
}
