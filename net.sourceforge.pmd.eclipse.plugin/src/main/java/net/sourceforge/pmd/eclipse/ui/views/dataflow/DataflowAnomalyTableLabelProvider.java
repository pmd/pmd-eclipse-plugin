/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.dataflow;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.reporting.RuleViolation;

/**
 * 
 * @author SebastianRaffel ( 07.06.2005 ), Sven Jacob
 */
public class DataflowAnomalyTableLabelProvider extends LabelProvider implements ITableLabelProvider {

    private static final String KEY_IMAGE_DFA = "error_dfa";

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        // set the Image for the anomaly
        Image image = null;
        if (columnIndex == 0) {
            image = PMDPlugin.getDefault().getImage(KEY_IMAGE_DFA, PMDUiConstants.ICON_LABEL_ERR_DFA);
        }
        return image;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof RuleViolation) {
            final RuleViolation violation = (RuleViolation) element;
            switch (columnIndex) {
            case 0:
                // show the Type of Anomaly which is saved as message here
                //return ((DaaRuleViolation) violation).getType();
                return "daa rule violation";

            case 1:
                // show the (first and last) Line
                int line1 = violation.getBeginLine();
                int line2 = violation.getEndLine();

                // show only one Line if they are equal
                if (line1 == line2 || line2 == 0) {
                    return String.valueOf(line1);
                } else {
                    // ... or twist them if needed and show something like "11, 12"
                    if (line2 < line1) {
                        final int temp = line1;
                        line1 = line2;
                        line2 = temp;
                    }
                    return line1 + ", " + line2;
                }

            case 2:
                return violation.getAdditionalInfo().get(RuleViolation.VARIABLE_NAME);
            case 3:
                return violation.getAdditionalInfo().get(RuleViolation.METHOD_NAME);

            default:
                return "";
            }
        }

        return "";
    }
}
