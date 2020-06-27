/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class visits all of the resources in the Eclipse Workspace, and runs PMD
 * on them if they happen to be Java files.
 *
 * <p>Any violations get tagged onto the file as problems in the tasks list.
 *
 * @author Philippe Herlin
 *
 */
public class ResourceVisitor extends BaseVisitor implements IResourceVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceVisitor.class);

    public boolean visit(final IResource resource) {
        LOG.debug("Visiting resource {}", resource.getName());
        boolean fVisitChildren = true;

        if (this.isCanceled()) {
            fVisitChildren = false;
        } else {
            this.reviewResource(resource);
        }

        return fVisitChildren;
    }

}
