/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

/**
 * 
 * @author Brian Remedios
 */
public final class ResourceManager {

    private Map<String, Image> imagesByCode = new HashMap<>();

    private static ResourceManager instance = new ResourceManager();

    private ResourceManager() {
    }

    public static Image imageFor(String codePath) {

        if (instance.imagesByCode.containsKey(codePath)) {
            return instance.imagesByCode.get(codePath);
        }

        ImageDescriptor desc = PMDPlugin.getImageDescriptor(codePath);
        if (desc == null) {
            System.out.println("no image for: " + codePath); // TODO handle better
            return null;
        }
        Image image = desc.createImage();
        instance.imagesByCode.put(codePath, image);
        return image;
    }

    public static void dispose() {

        for (Image image : instance.imagesByCode.values()) {
            image.dispose();
        }
    }
}
