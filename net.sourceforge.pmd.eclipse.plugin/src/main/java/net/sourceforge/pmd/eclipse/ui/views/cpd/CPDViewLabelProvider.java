/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.cpd;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.TokenEntry;

/**
 * 
 * 
 * @author Sven
 *
 */
public class CPDViewLabelProvider extends LabelProvider implements ITableLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        Image image = null;
        
        final TreeNode node = (TreeNode) element;
        final Object value = node.getValue();
        
        // the second Column gets an Image depending on,
        // if the Element is a Match or TokenEntry
        if (columnIndex == 1) {
            if (value instanceof Match) {
                image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
            } else if (value instanceof TokenEntry) {
                image = PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OPEN_MARKER);
            }
        }
        // otherwise
        // let the image null.

        return image;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        final TreeNode node = (TreeNode) element;
        final Object value = node.getValue();
        String result = "";

        switch (columnIndex) {
        // show the message 
        case 2:
            if (value instanceof Match) {
                final Match match = (Match) value;
                final StringBuilder buffer = new StringBuilder(50);
                buffer.append("Found suspect cut & paste (");
                buffer.append(match.getMarkCount()).append(" matches,");
                buffer.append(match.getLineCount());
                if (match.getLineCount() == 1) {
                    buffer.append(" line)");
                } else {
                    buffer.append(" lines)");
                }
                result = buffer.toString();
            } else if (value instanceof TokenEntry) {
                final TokenEntry entry = (TokenEntry) value;
                final Match match = (Match) node.getParent().getValue();
                final int startLine = entry.getBeginLine();
                final int endLine = entry.getBeginLine() + match.getLineCount() - 1;
                final IPath path = Path.fromOSString(entry.getTokenSrcID());
                final StringBuilder buffer = new StringBuilder(100);
                if (startLine == endLine) {
                    buffer.append("line ").append(startLine);
                } else {
                    buffer.append("lines ").append(startLine).append('-').append(endLine);
                }
                buffer.append(" in file ").append(path.lastSegment()); 
                result = buffer.toString();
            }
            break;
        case 3:
            if (value instanceof TokenEntry) {
                final TokenEntry entry = (TokenEntry) value;
                final IPath path = Path.fromOSString(entry.getTokenSrcID());
                final IResource resource = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(path);
                if (resource != null) {
                    result = resource.getProjectRelativePath().removeFileExtension().toString().replace(IPath.SEPARATOR, '.');
                }
            }
            break;
        default:
            // let text empty
        }

        return result;
    }

}
