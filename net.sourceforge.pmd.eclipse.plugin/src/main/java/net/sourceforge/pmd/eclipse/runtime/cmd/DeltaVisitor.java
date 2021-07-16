/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PMD visitor for processing resource deltas.
 * 
 * @author Philippe Herlin
 *
 */
public class DeltaVisitor extends BaseVisitor implements IResourceDeltaVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(DeltaVisitor.class);

    public DeltaVisitor() {
        super();
    }

    /**
     * Constructor with monitor.
     */
    public DeltaVisitor(IProgressMonitor monitor) {
        super();
        setMonitor(monitor);
    }

    @Override
    public boolean visit(IResourceDelta delta) throws CoreException {

        if (isCanceled()) {
            return false;
        }

        switch (delta.getKind()) {
        case IResourceDelta.ADDED: {
            LOG.debug("Visiting added resource " + delta.getResource().getName());
            visitAdded(delta.getResource());
            break;
        }
        case IResourceDelta.CHANGED: {
            LOG.debug("Visiting changed resource " + delta.getResource().getName());
            visitChanged(delta.getResource());
            break;
        }
        default: { // other kinds are not visited
            LOG.debug("Resource " + delta.getResource().getName() + " not visited.");
            break;
        }
        }

        return true;
    }

    /**
     * Visit added resource.
     * 
     * @param resource
     *            a new resource
     */
    private void visitAdded(IResource resource) {
        reviewResource(resource);
    }

    /**
     * Visit changed resource.
     * 
     * @param resource
     *            a changed resource
     */
    private void visitChanged(final IResource resource) {
        reviewResource(resource);
    }

}
