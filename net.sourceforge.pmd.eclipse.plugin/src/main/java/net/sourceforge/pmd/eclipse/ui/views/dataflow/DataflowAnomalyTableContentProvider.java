/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.dataflow;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import net.sourceforge.pmd.eclipse.util.Util;

/**
 * Provides the Content for the DataflowAnomalyTable.
 *
 * @author SebastianRaffel ( 07.06.2005 )
 */
public class DataflowAnomalyTableContentProvider implements IStructuredContentProvider {
    //private static final int MAX_ROWS = 200;

    @Override
    public Object[] getElements(Object inputElement) {
        //        if (inputElement instanceof Iterator<?>) {
        //            final Iterator<DaaRuleViolation> violationsIterator = (Iterator<DaaRuleViolation>) inputElement;
        //            final List<DaaRuleViolation> violations = new ArrayList<>();
        //            for (int count = 0; violationsIterator.hasNext() && count < MAX_ROWS; count++) {
        //                final DaaRuleViolation violation = violationsIterator.next();
        //                if (!violationIsInList(violation, violations)) {
        //                    violations.add(violation);
        //                }
        //            }
        //
        //            return violations.toArray(new DaaRuleViolation[0]);
        //        }
        return Util.EMPTY_ARRAY;
    }

    @Override
    public void dispose() {
        // do nothing
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // do nothing
    }

    //    private static boolean violationIsInList(DaaRuleViolation newViolation, List<DaaRuleViolation> list) {
    //        if (list.isEmpty()) {
    //            return false;
    //        }
    //
    //        final Iterator<DaaRuleViolation> violationIterator = list.iterator();
    //        while (violationIterator.hasNext()) {
    //
    //            final DaaRuleViolation violation = violationIterator.next();
    //            if (violation.getVariableName().equals(newViolation.getVariableName())
    //                    && violation.getType().equals(newViolation.getType())
    //                    && violation.getBeginLine() == newViolation.getBeginLine()
    //                    && violation.getEndLine() == newViolation.getEndLine()) {
    //                return true;
    //            }
    //        }
    //        return false;
    //    }

}
