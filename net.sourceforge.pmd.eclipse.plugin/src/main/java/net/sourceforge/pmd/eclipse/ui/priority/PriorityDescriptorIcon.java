/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.priority;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;

public final class PriorityDescriptorIcon {
    private final String iconId;
    private final String imagePath;

    private PriorityDescriptorIcon(String iconId, String imagePath) {
        this.iconId = iconId;
        this.imagePath = imagePath;
    }

    public String getIconId() {
        return iconId;
    }

    public Image getImage() {
        return PMDPlugin.getDefault().getImage(iconId, imagePath);
    }

    public static final PriorityDescriptorIcon[] ICONS = {
        new PriorityDescriptorIcon("icon-prio-1", "icons/prio_1.gif"),
        new PriorityDescriptorIcon("icon-prio-2", "icons/prio_2.gif"),
        new PriorityDescriptorIcon("icon-prio-3", "icons/prio_3.gif"),
        new PriorityDescriptorIcon("icon-prio-4", "icons/prio_4.gif"),
        new PriorityDescriptorIcon("icon-prio-5", "icons/prio_5.gif"),
        new PriorityDescriptorIcon("icon-pmd", "icons/pmd-icon-16.gif"),
        new PriorityDescriptorIcon("icon-error", "icons/error.gif"),
        new PriorityDescriptorIcon("icon-warn", "icons/warn.gif"),
        new PriorityDescriptorIcon("icon-info", "icons/info.gif"),
    };

    private static final Map<String, PriorityDescriptorIcon> ICONS_BY_ID;

    static {
        ICONS_BY_ID = new HashMap<>(ICONS.length);
        for (PriorityDescriptorIcon icon : ICONS) {
            ICONS_BY_ID.put(icon.getIconId(), icon);
        }
    }

    public static PriorityDescriptorIcon getById(String iconId) {
        return ICONS_BY_ID.get(iconId);
    }
}
