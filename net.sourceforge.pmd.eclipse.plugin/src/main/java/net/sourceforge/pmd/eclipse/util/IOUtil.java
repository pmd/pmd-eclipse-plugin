/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * 
 * @author Brian Remedios
 */
public class IOUtil {

    private IOUtil() {
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ex) {
            // ignore
        }
    }
}
