/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

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

    public static String getResourceAsString(IProject project, String resourceName) throws IOException, CoreException {
        IFile file = project.getFile(resourceName);
        String charset = file.getCharset();
        char[] buffer = new char[1024];
        StringBuilder result = new StringBuilder();
        try (Reader in = new InputStreamReader(file.getContents(), charset)) {
            int count = in.read(buffer);
            while (count > -1) {
                result.append(buffer, 0, count);
                count = in.read(buffer);
            }
            if (count > -1) {
                result.append(buffer, 0, count);
            }
        }
        return result.toString();
    }
}
