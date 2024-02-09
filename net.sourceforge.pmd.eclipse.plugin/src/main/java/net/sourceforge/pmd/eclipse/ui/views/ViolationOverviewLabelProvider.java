/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord;
import net.sourceforge.pmd.eclipse.ui.model.FileRecord;
import net.sourceforge.pmd.eclipse.ui.model.FileToMarkerRecord;
import net.sourceforge.pmd.eclipse.ui.model.MarkerRecord;
import net.sourceforge.pmd.eclipse.ui.model.PackageRecord;
import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptorCache;
import net.sourceforge.pmd.lang.rule.RulePriority;

/**
 * Provides the Violation Overview with Texts and Images
 *
 * @author SebastianRaffel ( 09.05.2005 ), Philippe Herlin
 *
 */
public class ViolationOverviewLabelProvider extends LabelProvider implements ITableLabelProvider {

    private static final String KEY_IMAGE_PACKAGE = "package";
    private static final String KEY_IMAGE_JAVAFILE = "javafile";

    private final ViolationOverview violationView;

    public ViolationOverviewLabelProvider(ViolationOverview overview) {
        super();
        violationView = overview;
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        Image image = null;

        // the first column
        if (columnIndex == 0) {
            if (element instanceof PackageRecord) {
                image = getImage(KEY_IMAGE_PACKAGE, PMDUiConstants.ICON_PACKAGE);
            } else if (element instanceof FileRecord || element instanceof FileToMarkerRecord) {
                image = getImage(KEY_IMAGE_JAVAFILE, PMDUiConstants.ICON_JAVACU);
            } else if (element instanceof MarkerRecord) {
                MarkerRecord markerRecord = (MarkerRecord) element;
                int priority = markerRecord.getPriority();
                // image must be 16x16 pixels, since the other images in the table are 16x16 as well (ICON_PACKAGE)
                image = PriorityDescriptorCache.INSTANCE.descriptorFor(RulePriority.valueOf(priority)).getImage(16);
            }
        }

        return image;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        String result = "";

        if (element instanceof AbstractPMDRecord) {
            final AbstractPMDRecord record = (AbstractPMDRecord) element;
            switch (columnIndex) {

            // show the Element's Name
            case 0:
                result = getElementName(element);
                break;

            // show the Number of Violations
            case 1:
                result = getNumberOfViolations(record);
                break;

            // show the Number of Violations per 1K lines of code
            case 2:
                result = getViolationsPerKLOC(record);
                break;

            // show the Number of Violations per Number of Methods
            case 3:
                result = getViolationsPerMethod(record);
                break;

            // show the Project's Name
            case 4:
                result = getProjectName(element);
                break;

            default:
                // let the result be an empty string
            }
        }
        return result;
    }

    /**
     * Gets the number of violation to an element.
     * 
     * @param element
     *            the record
     * @return number as string
     */
    private String getNumberOfViolations(AbstractPMDRecord element) {
        final int violations = this.violationView.getNumberOfFilteredViolations(element);
        return String.valueOf(violations);
    }

    /**
     * Return the name for the element column.
     *
     * @param element
     * @return
     */
    private String getElementName(Object element) {
        String name = "";
        if (element instanceof PackageRecord) {
            name = ((PackageRecord) element).getName();
        } else if (element instanceof FileRecord) {
            name = ((FileRecord) element).getName();
        } else if (element instanceof MarkerRecord) {
            name = ((MarkerRecord) element).getName();
        } else if (element instanceof FileToMarkerRecord) {
            name = ((FileToMarkerRecord) element).getParent().getParent().getName();
        }

        return name;
    }

    /**
     * Return the label for the Violations per LOC column.
     *
     * @param element
     * @return
     */
    private String getViolationsPerKLOC(AbstractPMDRecord element) {
        String result;
        int vioCount = violationView.getNumberOfFilteredViolations(element);
        int loc = violationView.getLOC(element);

        if (loc == 0) {
            result = "N/A";
        } else {
            double vioPerLoc = (double) (vioCount * 1000) / loc;
            if (vioPerLoc < 0.1) {
                result = "< 0.1";
            } else {
                DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                format.applyPattern("##0.0");
                result = format.format(vioPerLoc);
            }
        }

        return result;

    }

    /**
     * Return the label for the Violations per Method column.
     *
     * @param element
     * @return
     */
    private String getViolationsPerMethod(AbstractPMDRecord element) {
        String result;
        final int vioCount2 = violationView.getNumberOfFilteredViolations(element);
        final int numMethods = violationView.getNumberOfMethods(element);

        if (numMethods == 0) {
            result = "N/A";
        } else {
            final double vioPerMethod = (double) vioCount2 / numMethods;

            if (vioPerMethod < 0.01 || numMethods == 0) {
                result = "< 0.01";
            } else {
                final DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                format.applyPattern("##0.00");
                result = format.format(vioPerMethod);
            }
        }

        return result;
    }

    /**
     * Return the project name.
     *
     * @param element
     * @return
     */
    private String getProjectName(Object element) {
        String projectName = "";
        AbstractPMDRecord projectRec = null;
        if (element instanceof PackageRecord) {
            projectRec = ((PackageRecord) element).getParent();
        } else if (element instanceof FileRecord) {
            projectRec = ((FileRecord) element).getParent().getParent();
        } else if (element instanceof MarkerRecord) {
            projectRec = ((MarkerRecord) element).getParent().getParent().getParent();
        } else if (element instanceof FileToMarkerRecord) {
            projectRec = ((FileToMarkerRecord) element).getParent().getParent().getParent().getParent();
        }
        if (projectRec != null) {
            projectName = projectRec.getName();
        }
        return projectName;
    }

    /**
     * Helper method to get an image.
     *
     * @param key
     * @param iconPath
     * @return
     */
    private Image getImage(String key, String iconPath) {
        return PMDPlugin.getDefault().getImage(key, iconPath);
    }
}
