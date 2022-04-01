/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.util.internal;

public final class StringUtil {

    private StringUtil() {
        // utility
    }

    public static int maxCommonLeadingWhitespaceForAll(String[] lines) {
        int max = Integer.MAX_VALUE;

        for (String line : lines) {
            int current = 0;
            for (int i = 0; i < line.length() && Character.isWhitespace(line.charAt(i)); i++) {
                current++;
            }
            if (current < max) {
                max = current;
            }
        }

        return max;
    }

    public static String[] trimStartOn(String[] lines, int count) {
        if (count == 0) {
            return lines;
        }

        String[] result = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            result[i] = lines[i].substring(count);
        }
        return result;
    }
}
