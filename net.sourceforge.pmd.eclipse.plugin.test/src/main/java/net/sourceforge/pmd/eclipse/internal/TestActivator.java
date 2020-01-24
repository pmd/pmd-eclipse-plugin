/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

import ch.qos.logback.classic.Logger;

public class TestActivator extends AbstractUIPlugin {

    public TestActivator() {
        PMDPlugin mainPlugin = PMDPlugin.getDefault();
        Object l = LoggerFactory.getLogger("net.sourceforge.pmd");
        if (l instanceof Logger) {
            Logger logger = (Logger) l;
            logger.setAdditive(true);
        } else {
            mainPlugin.getLog().log(new Status(IStatus.WARNING, PMDPlugin.PLUGIN_ID, "Couldn't setup logging for tests."));
        }
        
    }

}
