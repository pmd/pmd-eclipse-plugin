/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingRule extends TestWatcher {

    @Override
    protected void starting(Description description) {
        final Logger LOG = LoggerFactory.getLogger(description.getClassName());
        LOG.debug("\n*\n*\n* Starting test {}\n*\n*\n*", description.getMethodName());
    }

    @Override
    protected void finished(Description description) {
        final Logger LOG = LoggerFactory.getLogger(description.getClassName());
        LOG.debug("\n*\n*\n* Finished test {}\n*\n*\n*", description.getMethodName());
    }
}
