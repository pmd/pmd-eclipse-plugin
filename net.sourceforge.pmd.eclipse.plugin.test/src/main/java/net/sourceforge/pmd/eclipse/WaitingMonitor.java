/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.NullProgressMonitor;

public class WaitingMonitor extends NullProgressMonitor {
    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void done() {
        super.done();
        latch.countDown();
    }

    public void await() throws InterruptedException {
        latch.await(30, TimeUnit.SECONDS);
    }
}
