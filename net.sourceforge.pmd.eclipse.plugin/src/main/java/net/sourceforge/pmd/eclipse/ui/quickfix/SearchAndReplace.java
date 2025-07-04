/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.quickfix;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchAndReplace extends AbstractFix {

    private final String replaceStr;
    private final Pattern pattern;

    public SearchAndReplace(String searchString, String replacement) {
        super("Search & replace");

        replaceStr = replacement;
        pattern = Pattern.compile(searchString);
    }

    @Override
    public String fix(String sourceCode, int lineNumber) {
        Matcher matcher = pattern.matcher(sourceCode);
        return matcher.replaceAll(replaceStr);
    }
}
