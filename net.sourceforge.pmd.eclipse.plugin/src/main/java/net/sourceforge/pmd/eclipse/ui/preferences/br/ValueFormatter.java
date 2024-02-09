/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.br;

import java.util.Date;

import net.sourceforge.pmd.eclipse.plugin.UISettings;
import net.sourceforge.pmd.eclipse.util.Util;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.rule.RulePriority;

/**
 *
 * @author Brian Remedios
 */
public interface ValueFormatter {

    ValueFormatter STRING_FORMATTER = new BasicValueFormatter(null) {
        @Override
        public void format(Object value, StringBuilder target) {
            target.append(value == null ? "" : value);
        }

        @Override
        public String format(Object value) {
            return value == null ? "" : value.toString();
        }
    };

    ValueFormatter MULTI_STRING_FORMATTER = new BasicValueFormatter(null) {
        @Override
        public void format(Object value, StringBuilder target) {
            target.append('[');
            Util.asString((Object[]) value, ", ", target);
            target.append(']');
        }
    };

    ValueFormatter NUMBER_FORMATTER = new BasicValueFormatter(null) {
        @Override
        public void format(Object value, StringBuilder target) {
            target.append(value == null ? "?" : value);
        }
    };

    ValueFormatter BOOLEAN_FORMATTER = new BasicValueFormatter(null) {
        @Override
        public void format(Object value, StringBuilder target) {
            target.append(value == null ? "?" : value);
        }
    };

    ValueFormatter TYPE_FORMATTER = new BasicValueFormatter(null) {
        @Override
        public void format(Object value, StringBuilder target) {
            target.append(value == null ? "" : ((Class<?>) value).getName());
        }
    };

    ValueFormatter OBJECT_FORMATTER = new BasicValueFormatter(null) {
        @Override
        public void format(Object value, StringBuilder target) {
            target.append(value == null ? "" : value);
        }
    };

    ValueFormatter OBJECT_ARRAY_FORMATTER = new BasicValueFormatter(null) {
        @Override
        public void format(Object value, StringBuilder target) {
            target.append('[');
            Util.asString((Object[]) value, ", ", target);
            target.append(']');
        }
    };

    // =================================================================

    ValueFormatter PRIORITY_FORMATTER = new BasicValueFormatter(null) {
        @Override
        public String format(Object value) {
            return UISettings.labelFor((RulePriority) value);
        }
    };

    ValueFormatter LANGUAGE_FORMATTER = new BasicValueFormatter(null) {
        @Override
        public String format(Object value) {
            return ((Language) value).getName();
        }
    };

    ValueFormatter LANGUAGE_VERSION_FORMATTER = new BasicValueFormatter(null) {
        @Override
        public String format(Object value) {
            return ((LanguageVersion) value).getName();
        }
    };

    ValueFormatter DATE_FROM_LONG_FORMATTER = new BasicValueFormatter("Date") {
        @Override
        public String format(Object value) {
            return new Date((Long) value).toString();
        }
    };

    ValueFormatter TIME_FROM_LONG_FORMATTER = new BasicValueFormatter("Time") {
        @Override
        public String format(Object value) {
            return new Date((Long) value).toString();
        }
    };

    ValueFormatter[] TIME_FORMATTERS = new ValueFormatter[] { DATE_FROM_LONG_FORMATTER };

    String format(Object value);

    void format(Object value, StringBuilder target);

}
