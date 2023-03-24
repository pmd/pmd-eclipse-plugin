/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.internal;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class TestActivator extends AbstractUIPlugin {

    public TestActivator() {
        PMDPlugin mainPlugin = PMDPlugin.getDefault();
        Object l = LoggerFactory.getLogger("net.sourceforge.pmd");
        if (l instanceof Logger) {
            Logger logger = (Logger) l;
            logger.setAdditive(true);
            logger.setLevel(Level.DEBUG);
        } else {
            mainPlugin.getLog().log(new Status(IStatus.WARNING, PMDPlugin.PLUGIN_ID, "Couldn't setup logging for tests."));
        }

        // Setup a default JVM. This is especially required for mac osx on github actions,
        // as there is no default JVM detected automatically.
        // This is not done via the following extension, in order to check for the existence of openjdk8Path:
        /*
   <extension
         point="org.eclipse.jdt.launching.vmInstalls">
      <vmInstall
            home="${env_var:HOME}/openjdk8"
            id="net.sourceforge.pmd.eclipse.plugin.test.openjdk8"
            name="openjdk8"
            vmInstallType="org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType">
      </vmInstall>
   </extension>
         */
        File openjdk8Path = new File(System.getProperty("user.home"), "openjdk8");
        if (openjdk8Path.exists()) {
            try {
                openjdk8Path = openjdk8Path.getCanonicalFile();
                IVMInstallType vmInstallType = JavaRuntime.getVMInstallType("org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType");
                IVMInstall vm = vmInstallType.createVMInstall("net.sourceforge.pmd.eclipse.plugin.test.openjdk8");
                vm.setInstallLocation(openjdk8Path);
                vm.setName("openjdk8");
                JavaRuntime.setDefaultVMInstall(vm, new NullProgressMonitor());
            } catch (IOException | CoreException e) {
                mainPlugin.getLog().log(new Status(IStatus.ERROR, PMDPlugin.PLUGIN_ID,
                        "Error setting up default JVM " + openjdk8Path, e));
                Thread.currentThread().interrupt();
            }
        }
    }
}
