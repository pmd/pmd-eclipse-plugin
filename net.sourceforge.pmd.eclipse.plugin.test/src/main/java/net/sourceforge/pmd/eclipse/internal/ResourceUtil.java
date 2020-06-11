/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceUtil {
    private ResourceUtil() {
        // utility
    }

    public static void copyResource(Object context, String resource, File target) throws IOException {
        try (FileOutputStream out = new FileOutputStream(target);
             InputStream in = context.getClass().getResourceAsStream(resource)) {
            int count;
            byte[] buffer = new byte[8192];
            count = in.read(buffer);
            while (count > -1) {
                out.write(buffer, 0, count);
                count = in.read(buffer);
            }
        }
    }
}
