
package net.sourceforge.pmd.eclipse.ui.preferences.br;

import org.eclipse.swt.SWT;

import net.sourceforge.pmd.eclipse.plugin.UISettings;
import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.util.StyledTextBuilder;
import net.sourceforge.pmd.eclipse.util.TextAsColourShapeBuilder;
import net.sourceforge.pmd.eclipse.util.UniqueItemsAsShapeBuilder;

/**
 * 
 * @author Brian Remedios
 */
public interface RuleTableColumns {

    RuleColumnDescriptor NAME = new TextColumnDescriptor("tName", StringKeys.PREF_RULESET_COLUMN_RULE_NAME, SWT.LEFT,
            210, RuleFieldAccessor.NAME, true, null);
    RuleColumnDescriptor RULE_SET_NAME = new TextColumnDescriptor("tRSName", StringKeys.PREF_RULESET_COLUMN_RULESET_NAME,
            SWT.LEFT, 160, TextColumnDescriptor.RULE_SET_NAME_ACCESSOR, true, null);
    RuleColumnDescriptor PRIORITY = new TextColumnDescriptor("tPriority", StringKeys.PREF_RULESET_COLUMN_PRIORITY,
            SWT.RIGHT, 53, RuleFieldAccessor.PRIORITY, false, null);
    RuleColumnDescriptor PRIORITY_NAME = new TextColumnDescriptor("tPrioName", StringKeys.PREF_RULESET_COLUMN_PRIORITY,
            SWT.LEFT, 80, RuleFieldAccessor.PRIORITY_NAME, true, null);
    RuleColumnDescriptor SINCE = new TextColumnDescriptor("tSince", StringKeys.PREF_RULESET_COLUMN_SINCE, SWT.RIGHT, 46,
            RuleFieldAccessor.SINCE, false, null);
    RuleColumnDescriptor USES_DFA = new TextColumnDescriptor("tUsesDFA", StringKeys.PREF_RULESET_COLUMN_DATAFLOW,
            SWT.LEFT, 60, RuleFieldAccessor.USES_DFA, false, null);
    RuleColumnDescriptor EXTERNAL_URL = new TextColumnDescriptor("tExtURL", StringKeys.PREF_RULESET_COLUMN_URL, SWT.LEFT,
            100, RuleFieldAccessor.URL, true, null);
    RuleColumnDescriptor PROOPERTIES = new TextColumnDescriptor("tProps", StringKeys.PREF_RULESET_COLUMN_PROPERTIES,
            SWT.LEFT, 40, TextColumnDescriptor.PROPERTIES_ACCESSOR, true, null);
    RuleColumnDescriptor LANGUAGE = new TextColumnDescriptor("tLang", StringKeys.PREF_RULESET_COLUMN_LANGUAGE, SWT.LEFT,
            32, RuleFieldAccessor.LANGUAGE, false, null);
    RuleColumnDescriptor RULE_TYPE = new TextColumnDescriptor("tRType", StringKeys.PREF_RULESET_COLUMN_RULE_TYPE,
            SWT.LEFT, 20, RuleFieldAccessor.RULE_TYPE, false, null);
    RuleColumnDescriptor MIN_LANGUAGE_VERSION = new TextColumnDescriptor("tMinLang", StringKeys.PREF_RULESET_COLUMN_MIN_VER,
            SWT.LEFT, 30, RuleFieldAccessor.MIN_LANGUAGE_VERSION, false, null);
    RuleColumnDescriptor MAX_LANGUAGE_VERSION = new TextColumnDescriptor("tMaxLang", StringKeys.PREF_RULESET_COLUMN_MAX_VER,
            SWT.LEFT, 30, RuleFieldAccessor.MAX_LANGUAGE_VERSION, false, null);
    RuleColumnDescriptor EXAMPLE_COUNT = new TextColumnDescriptor("tXmpCnt", StringKeys.PREF_RULESET_COLUMN_EXAMPLE_CNT,
            SWT.RIGHT, 20, RuleFieldAccessor.EXAMPLE_COUNT, false, null);
    RuleColumnDescriptor FIX_COUNT = new TextColumnDescriptor("fixCnt", StringKeys.PREF_RULESET_COLUMN_FIXCOUNT,
            SWT.RIGHT, 25, RuleFieldAccessor.FIX_COUNT, false, null);
    RuleColumnDescriptor MOD_COUNT = new TextColumnDescriptor("modCnt", StringKeys.PREF_RULESET_COLUMN_MODCOUNT,
            SWT.RIGHT, 25, RuleFieldAccessor.NON_DEFAULT_PROPERTY_COUNT, false, null);

    // RuleColumnDescriptor violateXPath = new TextColumnDescriptor("Filter",
    // SWT.RIGHT, 20, RuleFieldAccessor.violationXPath, true);

    RuleColumnDescriptor IMG_PRIORITY = new ImageColumnDescriptor("iPriority", StringKeys.PREF_RULESET_COLUMN_PRIORITY,
            SWT.LEFT, 50, RuleFieldAccessor.PRIORITY, false, PMDUiConstants.ICON_BUTTON_DIAMOND_WHITE,
            new UniqueItemsAsShapeBuilder(12, 12, SWT.LEFT, UISettings.shapesByPriority()));
    RuleColumnDescriptor FILTER_VIOLATION_REGEX = new ImageColumnDescriptor("iFvReg",
            StringKeys.PREF_RULESET_COLUMN_FILTERS_REGEX, SWT.LEFT, 25, RuleFieldAccessor.VIOLATION_REGEX, false,
            PMDUiConstants.ICON_FILTER_R, new TextAsColourShapeBuilder(16, 16, RuleUIUtil.REGEX_FILTER_SHAPE));
    RuleColumnDescriptor FILTER_VIOLATION_XPATH = new ImageColumnDescriptor("iFVXp",
            StringKeys.PREF_RULESET_COLUMN_FILTERS_XPATH, SWT.LEFT, 25, RuleFieldAccessor.VIOLATION_XPATH, false,
            PMDUiConstants.ICON_FILTER_X, new TextAsColourShapeBuilder(16, 16, RuleUIUtil.XPATH_FILTER_SHAPE));
    RuleColumnDescriptor IMG_PROPERTIES = new ImageColumnDescriptor("iProps", StringKeys.PREF_RULESET_COLUMN_PROPERTIES,
            SWT.LEFT, 40, ImageColumnDescriptor.PROPERTIES_ACCESSOR, false, null,
            new StyledTextBuilder(RuleUIUtil.CHANGED_PROPERTY_FONT));

    //RuleColumnDescriptor[] DEFAULT_HIDDEN_COLUMNS = new RuleColumnDescriptor[] {
    //    RuleTableColumns.EXTERNAL_URL, RuleTableColumns.MIN_LANGUAGE_VERSION, RuleTableColumns.FIX_COUNT,
    //    RuleTableColumns.EXAMPLE_COUNT, RuleTableColumns.MAX_LANGUAGE_VERSION, RuleTableColumns.SINCE,
    //    RuleTableColumns.MOD_COUNT };
    String DEFAULT_HIDDEN_COLUMNS_IDS = "tExtURL,tMinLang,fixCnt,tXmpCnt,tMaxLang,tSince,modCnt";

}
