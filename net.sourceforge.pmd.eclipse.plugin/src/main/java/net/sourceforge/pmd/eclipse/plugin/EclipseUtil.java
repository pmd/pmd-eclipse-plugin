/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.plugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * 
 * @author Brian Remedios
 * @deprecated will be removed since it is not needed
 */
@Deprecated
public final class EclipseUtil {
    /**
     * @deprecated Use {@link NullProgressMonitor} instead
     */
    @Deprecated
    public static final IProgressMonitor DUMMY_MONITOR = new IProgressMonitor() {

        @Override
        public void beginTask(String name, int totalWork) {
            // ignored
        }

        @Override
        public void done() {
            // ignored
        }

        @Override
        public void internalWorked(double work) {
            // ignored
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public void setCanceled(boolean value) {
            // ignored
        }

        @Override
        public void setTaskName(String name) {
            // ignored
        }

        @Override
        public void subTask(String name) {
            // ignored
        }

        @Override
        public void worked(int work) {
            // ignored
        }
    };

    private EclipseUtil() {
        // utility class
    }
}
