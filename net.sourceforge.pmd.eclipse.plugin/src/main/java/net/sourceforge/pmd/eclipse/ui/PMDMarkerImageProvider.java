/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptorCache;
import net.sourceforge.pmd.lang.rule.RulePriority;

public class PMDMarkerImageProvider implements IAnnotationImageProvider {

    @Override
    public ImageDescriptor getImageDescriptor(String imageDescriptorId) {
        return null;
    }

    @Override
    public String getImageDescriptorId(Annotation annotation) {
        return null;
    }

    @Override
    public Image getManagedImage(Annotation annotation) {
        String type = annotation.getType();
        RulePriority priority = RulePriority.HIGH;

        if ("net.sourceforge.pmd.eclipse.plugin.annotation.prio1".equals(type)) {
            priority = RulePriority.HIGH;
        } else if ("net.sourceforge.pmd.eclipse.plugin.annotation.prio2".equals(type)) {
            priority = RulePriority.MEDIUM_HIGH;
        } else if ("net.sourceforge.pmd.eclipse.plugin.annotation.prio3".equals(type)) {
            priority = RulePriority.MEDIUM;
        } else if ("net.sourceforge.pmd.eclipse.plugin.annotation.prio4".equals(type)) {
            priority = RulePriority.MEDIUM_LOW;
        } else if ("net.sourceforge.pmd.eclipse.plugin.annotation.prio5".equals(type)) {
            priority = RulePriority.LOW;
        }
        return PriorityDescriptorCache.INSTANCE.descriptorFor(priority).getAnnotationImage();
    }
}
