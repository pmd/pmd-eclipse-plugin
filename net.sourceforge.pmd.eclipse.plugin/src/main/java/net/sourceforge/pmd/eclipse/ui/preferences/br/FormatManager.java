/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.rule.RulePriority;

/**
 * 
 * @author Brian Remedios
 */
public final class FormatManager {

    private static final Map<Class<?>, ValueFormatter> FORMATTERS_BY_TYPE = new HashMap<>();

    private FormatManager() {
    }

    static { // used to render property values in short form in main table
        FORMATTERS_BY_TYPE.put(String.class, ValueFormatter.STRING_FORMATTER);
        FORMATTERS_BY_TYPE.put(String[].class, ValueFormatter.MULTI_STRING_FORMATTER);
        FORMATTERS_BY_TYPE.put(Boolean.class, ValueFormatter.BOOLEAN_FORMATTER);
        FORMATTERS_BY_TYPE.put(Boolean[].class, ValueFormatter.OBJECT_ARRAY_FORMATTER);
        FORMATTERS_BY_TYPE.put(Integer.class, ValueFormatter.NUMBER_FORMATTER);
        FORMATTERS_BY_TYPE.put(Integer[].class, ValueFormatter.OBJECT_ARRAY_FORMATTER);
        FORMATTERS_BY_TYPE.put(Long.class, ValueFormatter.NUMBER_FORMATTER);
        FORMATTERS_BY_TYPE.put(Long[].class, ValueFormatter.OBJECT_ARRAY_FORMATTER);
        FORMATTERS_BY_TYPE.put(Float.class, ValueFormatter.NUMBER_FORMATTER);
        FORMATTERS_BY_TYPE.put(Float[].class, ValueFormatter.OBJECT_ARRAY_FORMATTER);
        FORMATTERS_BY_TYPE.put(Double.class, ValueFormatter.NUMBER_FORMATTER);
        FORMATTERS_BY_TYPE.put(Double[].class, ValueFormatter.OBJECT_ARRAY_FORMATTER);
        FORMATTERS_BY_TYPE.put(Character.class, ValueFormatter.OBJECT_FORMATTER);
        FORMATTERS_BY_TYPE.put(Character[].class, ValueFormatter.OBJECT_ARRAY_FORMATTER);
        FORMATTERS_BY_TYPE.put(Class.class, ValueFormatter.TYPE_FORMATTER);
        FORMATTERS_BY_TYPE.put(Object[].class, ValueFormatter.OBJECT_ARRAY_FORMATTER);

        FORMATTERS_BY_TYPE.put(RulePriority.class, ValueFormatter.PRIORITY_FORMATTER);
        FORMATTERS_BY_TYPE.put(Language.class, ValueFormatter.LANGUAGE_FORMATTER);
        FORMATTERS_BY_TYPE.put(LanguageVersion.class, ValueFormatter.LANGUAGE_VERSION_FORMATTER);
    }

    public static ValueFormatter formatterFor(Class<?> type) {
        return FORMATTERS_BY_TYPE.get(type);
    }
}
