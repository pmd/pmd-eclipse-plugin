/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.properties.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.runtime.properties.IProjectProperties;

class ProjectPropertiesTimestampTupel {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectPropertiesTimestampTupel.class);

    static final String PROPERTIES_FILE = ".pmd";

    private final IProjectProperties projectProperties;
    private long lastReadTimestamp;

    ProjectPropertiesTimestampTupel(IProjectProperties projectProperties) {
        super();
        this.projectProperties = projectProperties;
        this.lastReadTimestamp = getModificationTimestamp();
    }

    IProjectProperties getProjectProperties() {
        return projectProperties;
    }

    IProject getProject() {
        return projectProperties.getProject();
    }

    boolean isOutOfSync() throws CoreException {
        IProject project = projectProperties.getProject();
        IFile propertiesFile = project.getFile(PROPERTIES_FILE);
        if (!propertiesFile.isSynchronized(IResource.DEPTH_ZERO)) {
            LOG.debug("File {} is out of sync... refreshing initiated", propertiesFile);
            // refresh the file, so that a later load content works
            propertiesFile.refreshLocal(IResource.DEPTH_ZERO, null);
        }

        long newTimestamp = getModificationTimestamp();
        LOG.debug("Comparing timestamps for {}: lastRead={}, new={}", propertiesFile, lastReadTimestamp, newTimestamp);
        if (newTimestamp != lastReadTimestamp) {
            lastReadTimestamp = newTimestamp;
            return true;
        }
        return false;
    }

    private long getModificationTimestamp() {
        IProject project = projectProperties.getProject();
        IFile propertiesFile = project.getFile(PROPERTIES_FILE);

        long result = 0L;
        try {
            File propertiesFileReal = propertiesFile.getLocation().toFile();
            if (propertiesFileReal.exists()) {
                // Note: File::lastModified() might be not accurate enough, there's this bug:
                // https://bugs.openjdk.java.net/browse/JDK-8177809
                FileTime filesLastMod = Files.getLastModifiedTime(propertiesFileReal.toPath());
                result = filesLastMod.toMillis();
                LOG.debug("File {} last mod: {}", propertiesFile, result);
            }
        } catch (IOException e) {
            LOG.debug("Error while reading file modification time: {}", e.toString());
        }
        return result;
    }
}
