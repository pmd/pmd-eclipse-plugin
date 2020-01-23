/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.logging.internal;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;

public class LogbackConfiguration {
    public static final String ROOT_LOG_ID = "net.sourceforge.pmd";
    private static final String PMD_ECLIPSE_APPENDER_NAME = "PMDEclipseAppender";

    private ILog getLog() {
        return PMDPlugin.getDefault().getLog();
    }

    public void configureLogback() {
        unconfigureLogback();
        LoggerContext logbackContext = getLogbackContext();
        if (logbackContext == null) {
            getLog().log(new Status(IStatus.WARNING, PMDPlugin.PLUGIN_ID,
                    "Couldn't find logback. PMD logging is not available!"));
            return;
        }

        EclipseLogAppender logbackEclipseAppender = new EclipseLogAppender(PMDPlugin.PLUGIN_ID, getLog());
        logbackEclipseAppender.setContext(logbackContext);
        logbackEclipseAppender.setName(PMDPlugin.PLUGIN_ID);
        logbackEclipseAppender.start();

        ch.qos.logback.classic.Logger l = logbackContext.getLogger(ROOT_LOG_ID);
        l.addAppender(logbackEclipseAppender);
    }

    public void unconfigureLogback() {
        LoggerContext logbackContext = getLogbackContext();
        if (logbackContext == null) {
            return;
        }

        ch.qos.logback.classic.Logger l = logbackContext.getLogger(ROOT_LOG_ID);
        l.detachAndStopAllAppenders();
    }

    private void configureLogs(String logFileName, String logLevel) {
        LoggerContext logbackContext = getLogbackContext();
        if (logbackContext == null) {
            return;
        }

        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(logbackContext);
        appender.setFile(logFileName);
        appender.setName(PMD_ECLIPSE_APPENDER_NAME);
        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
        triggeringPolicy.setContext(logbackContext);
        triggeringPolicy.setMaxFileSize("10MB");
        triggeringPolicy.start();
        appender.setTriggeringPolicy(triggeringPolicy);

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(logbackContext);
        rollingPolicy.setFileNamePattern(logFileName + ".%i");
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(1);
        rollingPolicy.setParent(appender);
        rollingPolicy.start();
        appender.setRollingPolicy(rollingPolicy);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(logbackContext);
        encoder.setPattern("%d{yyyy/MM/dd HH:mm:ss,SSS} %-5p %-32c{1} %m%n");
        encoder.start();
        appender.setEncoder(encoder);

        ThresholdFilter filter = new ThresholdFilter();
        filter.setContext(logbackContext);
        filter.setLevel(logLevel);
        filter.start();
        appender.addFilter(filter);

        appender.start();

        ch.qos.logback.classic.Logger rootLogger = logbackContext.getLogger(ROOT_LOG_ID);
        rootLogger.addAppender(appender);
    }

    public void applyLogPreferences(String logFileName, String logLevel) {
        LoggerContext logbackContext = getLogbackContext();
        if (logbackContext == null) {
            return;
        }

        ch.qos.logback.classic.Logger rootLogger = logbackContext.getLogger(ROOT_LOG_ID);
        Appender<ILoggingEvent> appender = rootLogger.getAppender(PMD_ECLIPSE_APPENDER_NAME);
        if (appender != null) {
            rootLogger.detachAppender(appender);
            appender.stop();
        }
        configureLogs(logFileName, logLevel);
    }

    private LoggerContext getLogbackContext() {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        int maxTries = 10;
        while (!(loggerFactory instanceof LoggerContext) && maxTries > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
            maxTries--;
            loggerFactory = LoggerFactory.getILoggerFactory();
        }
        if (loggerFactory instanceof LoggerContext) {
            return (LoggerContext) loggerFactory;
        }
        return null;
    }

}
