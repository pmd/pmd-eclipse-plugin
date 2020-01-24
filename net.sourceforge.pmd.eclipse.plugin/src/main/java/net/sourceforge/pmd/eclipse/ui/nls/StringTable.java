/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.nls;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.util.IOUtil;

/**
 * This class implements a string table. This let the UI loads all displayed
 * strings from national properties.
 * 
 * @author Herlin
 *
 */

public class StringTable {
    private static final Logger LOG = LoggerFactory.getLogger(StringTable.class);
    private Properties table = null;

    /**
     * Get a string from the string table from its key. Return the key if not
     * found.
     */
    public String getString(String key) {
        String string = null;
        final Properties table = getTable();
        if (table != null) {
            string = table.getProperty(key, key);
        }

        return string;
    }

    /**
     * Lazy load the string table
     * 
     * @return the string table
     */
    private Properties getTable() {

        if (table != null) {
            return table;
        }

        InputStream is = null;
        try {
            table = new Properties();
            final URL messageTableUrl = FileLocator.find(PMDPlugin.getDefault().getBundle(),
                    new Path("$nl$/messages.properties"), null);
            if (messageTableUrl != null) {
                is = messageTableUrl.openStream();
                table.load(is);
            }
        } catch (IOException e) {
            LOG.error("IO Exception when loading string table", e);
        } finally {
            IOUtil.closeQuietly(is);
        }

        return table;
    }

}
