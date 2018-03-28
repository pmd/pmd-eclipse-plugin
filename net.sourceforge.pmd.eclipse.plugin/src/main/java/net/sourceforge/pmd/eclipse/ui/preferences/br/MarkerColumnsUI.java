
package net.sourceforge.pmd.eclipse.ui.preferences.br;

import java.util.Comparator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.eclipse.runtime.builder.MarkerUtil;
import net.sourceforge.pmd.eclipse.ui.ItemColumnDescriptor;
import net.sourceforge.pmd.eclipse.ui.ItemFieldAccessor;
import net.sourceforge.pmd.eclipse.ui.ItemFieldAccessorAdapter;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptorCache;
import net.sourceforge.pmd.eclipse.util.Util;

/**
 * 
 * @author Brian Remedios
 */
public interface MarkerColumnsUI {

    Comparator<RulePriority> COMP_PRIORITY = new Comparator<RulePriority>() {
        public int compare(RulePriority a, RulePriority b) {
            return a.compareTo(b);
        }
    };

    ItemFieldAccessor<RulePriority, IMarker> PRIORITY_ACC = new ItemFieldAccessorAdapter<RulePriority, IMarker>(
            COMP_PRIORITY) {
        public RulePriority valueFor(IMarker marker) {
            int prio = MarkerUtil.rulePriorityFor(marker, 1);
            return RulePriority.valueOf(prio);
        }
    };

    ItemFieldAccessor<Image, IMarker> PRIORITY_IMG_ACC = new ItemFieldAccessorAdapter<Image, IMarker>(null) {
        final Display display = Display.getCurrent();

        public Image imageFor(IMarker marker) {
            RulePriority rp = PRIORITY_ACC.valueFor(marker);
            return PriorityDescriptorCache.INSTANCE.descriptorFor(rp).getImage(display);
        }
    };

    ItemFieldAccessor<Integer, IMarker> LINE_NO_ACC = new ItemFieldAccessorAdapter<Integer, IMarker>(Util.compInt) {
        public Integer valueFor(IMarker marker) {
            return (Integer) marker.getAttribute(IMarker.LINE_NUMBER, 0);
        }
    };

    ItemFieldAccessor<Long, IMarker> CREATED_ACC = new ItemFieldAccessorAdapter<Long, IMarker>(Util.compLong) {
        public Long valueFor(IMarker marker) {
            return MarkerUtil.createdOn(marker, -1);
        }
    };

    ItemFieldAccessor<Boolean, IMarker> DONE_ACC = new ItemFieldAccessorAdapter<Boolean, IMarker>(Util.compBool) {
        public Boolean valueFor(IMarker marker) {
            return MarkerUtil.doneState(marker, false);
        }
    };

    ItemFieldAccessor<String, IMarker> RULE_NAME_ACC = new ItemFieldAccessorAdapter<String, IMarker>(Util.compStr) {
        public String valueFor(IMarker marker) {
            return MarkerUtil.ruleNameFor(marker);
        }
    };

    ItemFieldAccessor<String, IMarker> MESSAGE_ACC = new ItemFieldAccessorAdapter<String, IMarker>(Util.compStr) {
        public String valueFor(IMarker marker) {
            return MarkerUtil.messageFor(marker, "??");
        }
    };

    ItemColumnDescriptor<Image, IMarker> PRIORITY = new ItemColumnDescriptor<Image, IMarker>("tPriority", "Priority",
            SWT.CENTER, 20, false, PRIORITY_IMG_ACC);
    ItemColumnDescriptor<Boolean, IMarker> DONE = new ItemColumnDescriptor<Boolean, IMarker>("tDone", "done", SWT.LEFT,
            50, false, DONE_ACC);
    ItemColumnDescriptor<Long, IMarker> CREATED = new ItemColumnDescriptor<Long, IMarker>("tCreated", "created",
            SWT.LEFT, 130, true, CREATED_ACC, ValueFormatter.TIME_FORMATTERS);
    ItemColumnDescriptor<String, IMarker> RULE_NAME = new ItemColumnDescriptor<String, IMarker>("tRuleName", "Rule",
            SWT.LEFT, 190, true, RULE_NAME_ACC);
    ItemColumnDescriptor<String, IMarker> MESSAGE = new ItemColumnDescriptor<String, IMarker>("tMsg",
            StringKeys.VIEW_OUTLINE_COLUMN_MESSAGE, SWT.LEFT, 260, true, MESSAGE_ACC);
    ItemColumnDescriptor<Integer, IMarker> LINE_NUMBER = new ItemColumnDescriptor<Integer, IMarker>("tLineNo",
            StringKeys.VIEW_OUTLINE_COLUMN_LINE, SWT.LEFT, 50, false, LINE_NO_ACC);

}
