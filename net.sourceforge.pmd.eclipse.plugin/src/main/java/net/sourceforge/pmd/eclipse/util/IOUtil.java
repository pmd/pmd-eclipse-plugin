/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * 
 * @author Brian Remedios
 * @deprecated use try-with-resources instead
 */
@Deprecated
public final class IOUtil {

    private IOUtil() {
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
            // ignore
        }
    }
}
