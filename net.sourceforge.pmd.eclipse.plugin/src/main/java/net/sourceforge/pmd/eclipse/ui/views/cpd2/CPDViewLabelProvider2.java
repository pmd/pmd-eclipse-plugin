/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.cpd2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.TokenEntry;

/**
 * 
 * 
 * @author Brian Remedios
 */

public class CPDViewLabelProvider2 extends LabelProvider implements ITableLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        Image image = null;

        final TreeNode node = (TreeNode) element;
        final Object value = node.getValue();

        // the second Column gets an Image depending on,
        // if the Element is a Match or TokenEntry
        if (columnIndex == 0) {
            if (value instanceof Match) {
                // image =
                // PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
            } else if (value instanceof TokenEntry) {
                image = PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OPEN_MARKER);
            }
        }

        // otherwise
        // let the image null.

        return image;
    }

    private int lineCountFor(TreeNode node) {
        Object source = node.getValue();

        if (source instanceof Match) {
            return node.getChildren().length;
        }

        return -1;
    }

    public static String pathFor(Mark entry) {
        final IPath path = Path.fromOSString(entry.getFilename());
        final IResource resource = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(path);
        if (resource != null) {
            return resource.getProjectRelativePath().removeFileExtension().toString().replace(IPath.SEPARATOR, '.');
        } else {
            return "?";
        }
    }

    public static Mark[] entriesFor(Match match) {

        Set<Mark> entrySet = match.getMarkSet();
        Mark[] entries = new Mark[entrySet.size()];
        entries = entrySet.toArray(entries);
        Arrays.sort(entries);

        return entries;
    }

    public static String[] sourcesFor(Match match) {

        Mark[] entries = entriesFor(match);

        String[] classNames = new String[entries.length];

        int i = 0;
        for (Mark entry : entries) {
            classNames[i++] = pathFor(entry);
        }

        return classNames;
    }

    public static Map<String, Mark> entriesByClassnameFor(Match match) {
        Mark[] entries = entriesFor(match);
        Map<String, Mark> entriesByName = new HashMap<>(entries.length);

        for (Mark entry : entries) {
            entriesByName.put(pathFor(entry), entry);
        }

        return entriesByName;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        final TreeNode node = (TreeNode) element;
        final Object value = node.getValue();
        String result = "";

        switch (columnIndex) {
        case 0:
            int count = lineCountFor(node);
            if (count > 0) {
                result = Integer.toString(count);
            }
            break;
        // show the source
        case 1:
            if (value instanceof String) {
                result = String.valueOf(value);
                if (result.endsWith("\r")) {
                    result = result.substring(0, result.length() - 1);
                }
            }
            if (value instanceof Match) {
                // do nothing, let the painter show it
            }
            break;
        default:
            // let text empty
        }

        return result;
    }

}
