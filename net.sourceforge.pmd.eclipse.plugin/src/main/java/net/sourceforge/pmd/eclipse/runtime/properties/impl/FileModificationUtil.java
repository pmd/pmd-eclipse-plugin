/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.properties.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FileModificationUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FileModificationUtil.class);

    private FileModificationUtil() {
        // utility
    }

    static long getFileModificationTimestamp(File f) {
        long result = 0L;
        if (f.exists()) {
            // Note: File::lastModified() might be not accurate enough, there's this bug:
            // https://bugs.openjdk.java.net/browse/JDK-8177809
            FileTime filesLastMod;
            try {
                filesLastMod = Files.getLastModifiedTime(f.toPath());
                result = filesLastMod.toMillis();
                LOG.debug("File {} last mod: {}", f, result);
            } catch (IOException e) {
                LOG.debug("Error while reading file modification timestamp for {}: {}", f, e.toString());
            }
        }
        return result;
    }
}
