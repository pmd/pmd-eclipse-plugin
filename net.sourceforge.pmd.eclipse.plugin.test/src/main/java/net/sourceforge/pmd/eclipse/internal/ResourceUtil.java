/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public final class ResourceUtil {
    private ResourceUtil() {
        // utility
    }

    public static void copyResource(Object context, String resource, File target) throws IOException {
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (OutputStream out = Files.newOutputStream(target.toPath());
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
