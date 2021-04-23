/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.quickfix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.runtime.builder.MarkerUtil;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;

/**
 * Implementation of a resolution generator to bring the quick fixes feature of Eclipse to PMD
 *
 * @author Philippe Herlin, Brian Remedios
 *
 *         TODO resource bundles are read-only, migrate to a persistence mechanism that allows for updates to the fixes
 *         associated with the rules.
 */
public class PMDResolutionGenerator implements IMarkerResolutionGenerator {

    private static final Map<String, Fix[]> FIXERS_BY_RULE_NAME = new HashMap<String, Fix[]>();

    private static final Set<String> MISSING_FIXES = new HashSet<String>();
    private static final Map<String, String> BROKEN_FIXES = new HashMap<String, String>();

    private static final String QUICKFIX_BUNDLE = "properties.QuickFix"; // NOPMD

    public static final IMarkerResolution[] EMPTY_RESOLUTIONS = new IMarkerResolution[0];

    public static Class<? extends Fix> fixClassFor(String className, String ruleName) {

        if (StringUtils.isBlank(className)) {
            return null;
        }

        try {
            Class<?> cls = Class.forName(className);
            if (Fix.class.isAssignableFrom(cls)) {
                return cls.asSubclass(Fix.class);
            } else {
                BROKEN_FIXES.put(ruleName, className);
                return null;
            }
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    private static void add(String ruleName, Fix fix) {

        if (FIXERS_BY_RULE_NAME.containsKey(ruleName)) {
            Fix[] existingFixers = FIXERS_BY_RULE_NAME.get(ruleName);
            Fix[] newFixers = new Fix[existingFixers.length + 1];
            System.arraycopy(existingFixers, 0, newFixers, 0, existingFixers.length);
            newFixers[newFixers.length - 1] = fix;
            FIXERS_BY_RULE_NAME.put(ruleName, newFixers);
        } else {
            FIXERS_BY_RULE_NAME.put(ruleName, new Fix[] { fix });
        }
    }

    public static int fixCountFor(Rule rule) {

        String ruleName = rule.getName();
        if (MISSING_FIXES.contains(ruleName)) {
            return 0;
        }

        loadFixesFor(ruleName);

        if (!FIXERS_BY_RULE_NAME.containsKey(ruleName)) {
            return 0;
        }
        return FIXERS_BY_RULE_NAME.get(ruleName).length;
    }

    public static void saveFixesFor(String ruleName) {
        // TODO
    }

    private static void loadFixesFor(String ruleName) {

        ResourceBundle bundle = ResourceBundle.getBundle(QUICKFIX_BUNDLE);
        if (!bundle.containsKey(ruleName)) {
            MISSING_FIXES.add(ruleName);
            return;
        }

        String fixClassNameSet = bundle.getString(ruleName);
        String[] fixClassNames = fixClassNameSet.split(",");

        for (String fixClassName : fixClassNames) {
            if (StringUtils.isBlank(fixClassName)) {
                continue;
            }
            Class<? extends Fix> fixClass = fixClassFor(fixClassName.trim(), ruleName);
            if (fixClass != null) {
                Fix fix = fixFor(ruleName, fixClass);
                if (fix != null) {
                    add(ruleName, fix);
                }
            }
        }

        if (!FIXERS_BY_RULE_NAME.containsKey(ruleName)) {
            MISSING_FIXES.add(ruleName);
        }
    }

    public static boolean hasFixesFor(Rule rule) {

        String ruleName = rule.getName();
        if (FIXERS_BY_RULE_NAME.containsKey(ruleName)) {
            return true;
        }

        if (MISSING_FIXES.contains(ruleName)) {
            return false;
        }
        if (BROKEN_FIXES.containsKey(ruleName)) {
            return false;
        }

        loadFixesFor(ruleName);

        return FIXERS_BY_RULE_NAME.containsKey(ruleName);
    }

    private static Fix fixFor(String ruleName, Class<? extends Fix> fixClass) {

        try {
            return fixClass.getConstructor().newInstance();
        } catch (Exception ex) {
            BROKEN_FIXES.put(ruleName, fixClass.getName());
            return null;
        }
    }

    public static Fix[] fixesFor(Rule rule) {
        return FIXERS_BY_RULE_NAME.get(rule.getName());
    }

    public static void fixesFor(Rule rule, Fix[] fixes) {
        FIXERS_BY_RULE_NAME.put(rule.getName(), fixes);
    }

    /**
     * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
     */
    public IMarkerResolution[] getResolutions(IMarker marker) {

        final List<PMDResolution> markerResolutionList = new ArrayList<PMDResolution>();
        try {
            final String ruleName = MarkerUtil.ruleNameFor(marker);
            if (ruleName != null) {
                final RuleSet ruleSet = PMDPlugin.getDefault().getPreferencesManager().getRuleSet();
                final Rule rule = ruleSet.getRuleByName(ruleName);
                if (rule == null || !hasFixesFor(rule)) {
                    return EMPTY_RESOLUTIONS;
                }

                Fix[] fixes = fixesFor(rule);
                for (Fix fix : fixes) {
                    markerResolutionList.add(new PMDResolution(fix));
                }
            }
        } catch (RuntimeException e) {
            PMDPlugin.getDefault().showError(
                    PMDPlugin.getDefault().getStringTable().getString(StringKeys.ERROR_RUNTIME_EXCEPTION), e);
        }

        return markerResolutionList.toArray(new IMarkerResolution[markerResolutionList.size()]);
    }

}
