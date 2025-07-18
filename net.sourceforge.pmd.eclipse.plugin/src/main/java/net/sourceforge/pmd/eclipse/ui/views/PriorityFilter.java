/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import net.sourceforge.pmd.eclipse.plugin.PMDPlugin;
import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.model.AbstractPMDRecord;
import net.sourceforge.pmd.eclipse.ui.model.FileRecord;
import net.sourceforge.pmd.eclipse.ui.model.FileToMarkerRecord;
import net.sourceforge.pmd.eclipse.ui.model.MarkerRecord;
import net.sourceforge.pmd.eclipse.ui.model.PackageRecord;
import net.sourceforge.pmd.eclipse.ui.nls.StringKeys;
import net.sourceforge.pmd.lang.rule.RulePriority;

/**
 * The ViewerFilter for Priorities.
 * This is used for both Violation Outline and Violation Overview.
 *
 * <p>The current enabled filters are saved automatically in the preferences.
 * See {@link #PREFERENCE_KEY}.
 *
 * @author SebastianRaffel ( 17.05.2005 )
 */
public final class PriorityFilter extends ViewerFilter {
    private static final PriorityFilter INSTANCE = new PriorityFilter();

    private static final String PREFERENCE_KEY = PriorityFilter.class.getName() + ".enabledPriorities";

    private final Set<RulePriority> enabledPriorities;

    private Set<PriorityFilterChangeListener> listeners = new CopyOnWriteArraySet<>();

    private final ScopedPreferenceStore editorsPreferences = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.ui.editors");

    /**
     * Constructor
     *
     * @author SebastianRaffel ( 29.06.2005 )
     */
    private PriorityFilter() {
        enabledPriorities = Collections.synchronizedSet(EnumSet.allOf(RulePriority.class));
    }

    public static PriorityFilter getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the priority filter by loading the preferences and making sure, that
     * any change to the filter is stored to the preferences.
     */
    public void initialize() {
        addPriorityFilterChangeListener(new PriorityFilterChangeListener() {
            @Override
            public void priorityEnabled(RulePriority priority) {
                saveToPreferenceStore();
                showMarkers(priority);
            }

            @Override
            public void priorityDisabled(RulePriority priority) {
                saveToPreferenceStore();
                hideMarkers(priority);
            }
        });
        loadFromPreferenceStore();
    }

    private void saveToPreferenceStore() {
        StringBuilder asString = new StringBuilder(50);
        for (RulePriority rulePriority : enabledPriorities) {
            asString.append(rulePriority.name()).append(',');
        }
        PMDPlugin.getDefault().getPreferenceStore().setValue(PREFERENCE_KEY, asString.toString());
    }

    private void loadFromPreferenceStore() {
        String priorities = PMDPlugin.getDefault().getPreferenceStore().getString(PREFERENCE_KEY);
        if (priorities != null && !priorities.isEmpty()) {
            enabledPriorities.clear();
            for (String priority : priorities.split(",")) {
                enabledPriorities.add(RulePriority.valueOf(priority));
            }
        }
    }

    private String getMarkerKeyVerticalRuler(RulePriority priority) {
        return "net.sourceforge.pmd.eclipse.plugin.annotation.prio" + priority.getPriority() + ".verticalruler";
    }

    private String getMarkerKeyOverviewRuler(RulePriority priority) {
        return "net.sourceforge.pmd.eclipse.plugin.annotation.prio" + priority.getPriority() + ".overviewruler";
    }

    private void showMarkers(RulePriority priority) {
        editorsPreferences.setValue(getMarkerKeyVerticalRuler(priority), true);
        editorsPreferences.setValue(getMarkerKeyOverviewRuler(priority), true);
    }

    private void hideMarkers(RulePriority priority) {
        editorsPreferences.setValue(getMarkerKeyVerticalRuler(priority), false);
        editorsPreferences.setValue(getMarkerKeyOverviewRuler(priority), false);
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        boolean select = false;

        if (element instanceof PackageRecord) {
            // ViolationOverview
            select = hasMarkersToShow((PackageRecord) element);
        } else if (element instanceof FileRecord) {
            // ViolationOverview
            select = hasMarkersToShow((FileRecord) element);
        } else if (element instanceof IMarker) {
            // ViolationOutline
            try {
                final IMarker marker = (IMarker) element;
                final Integer markerPrio = (Integer) marker.getAttribute(PMDUiConstants.KEY_MARKERATT_PRIORITY);
                select = isPriorityEnabled(markerPrio);
            } catch (CoreException ce) {
                PMDPlugin.getDefault().logError(StringKeys.ERROR_CORE_EXCEPTION + this.toString(), ce);
            }
        } else if (element instanceof MarkerRecord) {
            // ViolationOverview
            final MarkerRecord markerRec = (MarkerRecord) element;
            select = isPriorityEnabled(markerRec.getPriority());
        } else if (element instanceof FileToMarkerRecord) {
            select = true;
        }
        return select;
    }

    private boolean isPriorityEnabled(Integer markerPrio) {
        boolean isEnabled = false;
        // for some unknown reasons markerPrio may be null.
        if (markerPrio != null) {
            isEnabled = isPriorityEnabled(RulePriority.valueOf(markerPrio));
        }
        return isEnabled;
    }

    public boolean isPriorityEnabled(RulePriority priority) {
        return enabledPriorities.contains(priority);
    }

    private boolean hasMarkersToShow(AbstractPMDRecord record) {
        boolean hasMarkers = false;
        for (RulePriority priority : enabledPriorities) {
            final IMarker[] markers = record.findMarkersByAttribute(PMDUiConstants.KEY_MARKERATT_PRIORITY, priority.getPriority());
            if (markers.length > 0) {
                hasMarkers = true;
                break;
            }
        }
        return hasMarkers;
    }

    public void enablePriority(RulePriority priority) {
        if (priority != null) {
            if (enabledPriorities.add(priority)) {
                notifyPriorityEnabled(priority);
            }
        }
    }

    public void disablePriority(RulePriority priority) {
        if (priority != null) {
            if (enabledPriorities.remove(priority)) {
                notifyPriorityDisabled(priority);
            }
        }
    }

    public void addPriorityFilterChangeListener(PriorityFilterChangeListener listener) {
        listeners.add(listener);
    }

    public void removePriorityFilterChangeListener(PriorityFilterChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyPriorityEnabled(RulePriority priority) {
        for (PriorityFilterChangeListener listener : listeners) {
            listener.priorityEnabled(priority);
        }
    }

    private void notifyPriorityDisabled(RulePriority priority) {
        for (PriorityFilterChangeListener listener : listeners) {
            listener.priorityDisabled(priority);
        }
    }

    public interface PriorityFilterChangeListener {
        void priorityEnabled(RulePriority priority);

        void priorityDisabled(RulePriority priority);
    }
}
