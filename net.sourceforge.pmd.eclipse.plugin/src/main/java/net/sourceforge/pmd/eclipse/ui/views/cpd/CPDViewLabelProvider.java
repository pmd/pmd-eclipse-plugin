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

import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.eclipse.runtime.cmd.internal.CpdMarkWithSourceCode;
import net.sourceforge.pmd.eclipse.runtime.cmd.internal.CpdMatchWithSourceCode;
import net.sourceforge.pmd.lang.document.FileLocation;

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
            if (value instanceof CpdMatchWithSourceCode) {
                image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
            } else if (value instanceof CpdMarkWithSourceCode) {
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
            if (value instanceof CpdMatchWithSourceCode) {
                final CpdMatchWithSourceCode data = (CpdMatchWithSourceCode) value;
                final Match match = data.getMatch();
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
            } else if (value instanceof CpdMarkWithSourceCode) {
                final CpdMarkWithSourceCode data = (CpdMarkWithSourceCode) value;
                final Mark entry = data.getMark();
                final FileLocation location = entry.getLocation();
                final Match match = ((CpdMatchWithSourceCode) node.getParent().getValue()).getMatch();
                final int startLine = location.getStartLine();
                final int endLine = location.getStartLine() + match.getLineCount() - 1;
                final IPath path = Path.fromOSString(location.getFileId().getOriginalPath());
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
            if (value instanceof CpdMarkWithSourceCode) {
                final CpdMarkWithSourceCode data = (CpdMarkWithSourceCode) value;
                final IPath path = Path.fromOSString(data.getMark().getLocation().getFileId().getOriginalPath());
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
