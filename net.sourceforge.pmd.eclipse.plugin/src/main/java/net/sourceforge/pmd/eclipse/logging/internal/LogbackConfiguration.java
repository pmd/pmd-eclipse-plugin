/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.logging.internal;

import java.lang.reflect.Method;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;

public class LogbackConfiguration {
    public static final String ROOT_LOG_ID = "net.sourceforge.pmd";
    private static final String PMD_ECLIPSE_APPENDER_NAME = "PMDEclipseAppender";

    private static final String DEFAULT_PMD_LOG_MAX_FILE_SIZE = "10MB";

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
        ThresholdFilter filter = new ThresholdFilter();
        filter.setContext(logbackContext);
        filter.setLevel(Level.INFO.toString());
        filter.start();
        logbackEclipseAppender.addFilter(filter);
        logbackEclipseAppender.start();

        Logger l = logbackContext.getLogger(ROOT_LOG_ID);
        l.addAppender(logbackEclipseAppender);
        l.setAdditive(false);

        JulLoggingHandler.install();
    }

    public void unconfigureLogback() {
        LoggerContext logbackContext = getLogbackContext();
        if (logbackContext == null) {
            return;
        }

        Logger l = logbackContext.getLogger(ROOT_LOG_ID);
        l.detachAndStopAllAppenders();

        JulLoggingHandler.uninstall();
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

        /*
         * SizeBaseTriggeringPolicy#setMaxFileSize changed between logback-core 1.1.7 and 1.1.8:
         *
         * https://github.com/qos-ch/logback/blob/v_1.1.7/logback-core/src/main/java/ch/qos/logback/core/rolling/SizeBasedTriggeringPolicy.java
         *
         *      public void setMaxFileSize(String maxFileSize)
         *
         * https://github.com/qos-ch/logback/blob/v_1.1.8/logback-core/src/main/java/ch/qos/logback/core/rolling/SizeBasedTriggeringPolicy.java
         *
         *      public void setMaxFileSize(FileSize aMaxFileSize)
         */
        Exception firstTry = null;
        try {
            Method m = triggeringPolicy.getClass().getMethod("setMaxFileSize", String.class);
            m.invoke(triggeringPolicy, DEFAULT_PMD_LOG_MAX_FILE_SIZE);
        } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
            firstTry = e;
        }

        if (firstTry != null) {
            try {
                Method m = triggeringPolicy.getClass().getMethod("setMaxFileSize", FileSize.class);
                m.invoke(triggeringPolicy, FileSize.valueOf(DEFAULT_PMD_LOG_MAX_FILE_SIZE));
            } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
                // relying on the default max file size
                System.err.println("WARNING: Unable to configure max file size for SizeBasedTriggeringPolicy.");
                System.err.println("Falling back to default of " + SizeBasedTriggeringPolicy.DEFAULT_MAX_FILE_SIZE + " bytes");
                System.err.println("Reported exception on first try:");
                firstTry.printStackTrace();
                System.err.println("Reported exception on second try:");
                e.printStackTrace();
            }
        }

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
        encoder.setPattern("%d{yyyy/MM/dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        encoder.start();
        appender.setEncoder(encoder);

        ThresholdFilter filter = new ThresholdFilter();
        filter.setContext(logbackContext);
        filter.setLevel(logLevel);
        filter.start();
        appender.addFilter(filter);

        appender.start();

        Logger rootLogger = logbackContext.getLogger(ROOT_LOG_ID);
        rootLogger.addAppender(appender);
        rootLogger.setLevel(Level.toLevel(logLevel, Level.INFO));
    }

    public void applyLogPreferences(String logFileName, String logLevel) {
        LoggerContext logbackContext = getLogbackContext();
        if (logbackContext == null) {
            return;
        }

        Logger rootLogger = logbackContext.getLogger(ROOT_LOG_ID);
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
