/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.logging.internal;

import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This logger is needed in order to channel PMD's own logs through eclipse/logback.
 */
public class JulLoggingHandler extends Handler {

    public static void install() {
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(LogbackConfiguration.ROOT_LOG_ID);
        for (Handler handler : julLogger.getHandlers()) {
            if (handler instanceof JulLoggingHandler) {
                return; // already installed
            }
        }
        julLogger.addHandler(new JulLoggingHandler());
        julLogger.setUseParentHandlers(false);
    }

    public static void uninstall() {
        JulLoggingHandler julLoggingHandler = null;
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(LogbackConfiguration.ROOT_LOG_ID);
        for (Handler handler : julLogger.getHandlers()) {
            if (handler instanceof JulLoggingHandler) {
                julLoggingHandler = (JulLoggingHandler) handler;
                break;
            }
        }
        julLogger.removeHandler(julLoggingHandler);
        julLogger.setUseParentHandlers(true);
    }

    @Override
    public void publish(LogRecord record) {
        if (record == null) {
            return;
        }

        String loggerName = record.getLoggerName();
        if (loggerName == null) {
            loggerName = LogbackConfiguration.ROOT_LOG_ID;
        }
        String message = record.getMessage();
        if (message == null) {
            message = "";
        }
        Object[] parameters = record.getParameters();
        if (parameters != null) {
            message = MessageFormat.format(message, parameters);
        }
        Throwable thrown = record.getThrown();
        int loglevel = record.getLevel().intValue();

        Logger slf4j = LoggerFactory.getLogger(loggerName);

        if (loglevel <= Level.FINE.intValue()) {
            slf4j.debug(message, thrown);
        } else if (loglevel <= Level.INFO.intValue()) {
            slf4j.info(message, thrown);
        } else if (loglevel <= Level.WARNING.intValue()) {
            slf4j.warn(message, thrown);
        } else {
            slf4j.error(message, thrown);
        }
    }

    @Override
    public void close() throws SecurityException {
        // empty
    }

    @Override
    public void flush() {
        // empty
    }
}
