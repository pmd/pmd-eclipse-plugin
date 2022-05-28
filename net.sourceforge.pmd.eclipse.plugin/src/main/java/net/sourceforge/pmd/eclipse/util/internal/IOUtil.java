/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.util.internal;

import java.io.IOException;
import java.io.Reader;

public final class IOUtil {

    private IOUtil() {}

    public static String toString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[8192];
        int count = reader.read(buffer);
        while (count > -1) {
            sb.append(buffer, 0, count);
            count = reader.read(buffer);
        }
        return sb.toString();
    }
}
