/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.logging.internal;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class EclipseLogAppender extends AppenderBase<ILoggingEvent> {
    private final ILog eclipseLog;
    private final String pluginId;

    public EclipseLogAppender(String thePluginId, ILog theEclipseLog) {
        this.pluginId = thePluginId;
        this.eclipseLog = theEclipseLog;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        int statusSeverity = convertLevel(eventObject.getLevel());
        Throwable throwable = getThrowable(eventObject.getArgumentArray());

        this.eclipseLog.log(new Status(statusSeverity, pluginId, eventObject.getFormattedMessage(), throwable));
    }

    private Throwable getThrowable(Object[] argumentArray) {
        if (argumentArray != null && argumentArray.length > 0) {
            Object last = argumentArray[argumentArray.length - 1];
            if (last instanceof Throwable) {
                return (Throwable) last;
            }
        }
        return null;
    }

    private int convertLevel(Level level) {
        switch (level.levelInt) {
        case Level.ERROR_INT:
            return IStatus.ERROR;
        case Level.WARN_INT:
            return IStatus.WARNING;
        case Level.INFO_INT:
            return IStatus.INFO;
        default:
            return IStatus.OK;
        }
    }
}
