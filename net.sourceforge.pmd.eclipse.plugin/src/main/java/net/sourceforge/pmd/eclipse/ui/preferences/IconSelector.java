/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import net.sourceforge.pmd.eclipse.ui.priority.PriorityDescriptorIcon;

public class IconSelector {
    private Composite composite;
    private ListenerList listeners = new ListenerList();
    private PriorityDescriptorIcon selectedIcon;

    public IconSelector(Composite parent) {
        composite = new Composite(parent, SWT.FILL);
        composite.setLayout(new FillLayout());

        final List<Button> buttonGroup = new ArrayList<>();
        for (PriorityDescriptorIcon icon : PriorityDescriptorIcon.ICONS) {
            Button button = new Button(composite, SWT.TOGGLE);
            button.setImage(icon.getImage());
            button.setData(icon);
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Button btn = (Button) e.widget;
                    if (btn.getSelection()) {
                        for (Button otherButton : buttonGroup) {
                            if (!Objects.equals(otherButton, btn)) {
                                otherButton.setSelection(false);
                            }
                        }
                        setSelectedIcon((PriorityDescriptorIcon) btn.getData());
                    } else {
                        btn.setSelection(true);
                    }
                }
            });
            buttonGroup.add(button);
        }

        addListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                PriorityDescriptorIcon icon = (PriorityDescriptorIcon) event.getNewValue();
                for (Button btn : buttonGroup) {
                    btn.setSelection(Objects.equals(btn.getData(), icon));
                }
            }
        });
    }

    public void setLayoutData(Object layoutData) {
        composite.setLayoutData(layoutData);
    }

    public void setSelectedIcon(PriorityDescriptorIcon icon) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, "selectedIcon", this.selectedIcon, icon);
        this.selectedIcon = icon;

        for (Object listener : listeners.getListeners()) {
            ((IPropertyChangeListener) listener).propertyChange(event);
        }
    }

    public PriorityDescriptorIcon getSelectedIcon() {
        return selectedIcon;
    }

    public void addListener(IPropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IPropertyChangeListener listener) {
        listeners.remove(listener);
    }
}
