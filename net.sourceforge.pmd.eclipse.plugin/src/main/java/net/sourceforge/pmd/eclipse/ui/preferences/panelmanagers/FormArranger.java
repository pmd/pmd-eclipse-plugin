/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.panelmanagers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.sourceforge.pmd.eclipse.ui.PMDUiConstants;
import net.sourceforge.pmd.eclipse.ui.dialogs.NewPropertyDialog;
import net.sourceforge.pmd.eclipse.ui.preferences.br.EditorFactory;
import net.sourceforge.pmd.eclipse.ui.preferences.br.RuleSelection;
import net.sourceforge.pmd.eclipse.ui.preferences.br.RuleUtil;
import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.internal.PropertyEditorFactory;
import net.sourceforge.pmd.eclipse.util.ResourceManager;
import net.sourceforge.pmd.eclipse.util.Util;
import net.sourceforge.pmd.eclipse.util.internal.SWTUtil;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * Takes in a property source instance, extracts its properties, creates a series of editors for each, and
 * then populates them with the current values. As some types can hold multiple values the vertical span can grow to
 * accommodate additional widgets and does so by broadcasting this through the SizeChange listener. The ValueChange
 * listener can be used to update any outside UIs as necessary.
 *
 * @author Brian Remedios
 */
public class FormArranger implements ValueChangeListener {
    private final Composite parent;
    private final ValueChangeListener changeListener;
    private final SizeChangeListener sizeChangeListener;
    private PropertySource propertySource;
    private Control[][] widgets;

    private Map<PropertyDescriptor<?>, Control[]> controlsByProperty;

    public FormArranger(Composite theParent, ValueChangeListener listener, SizeChangeListener sizeListener) {
        parent = theParent;
        changeListener = chain(listener, this);
        sizeChangeListener = sizeListener;

        controlsByProperty = new HashMap<>();
    }

    /**
     * @deprecated Use {@link #FormArranger(Composite, ValueChangeListener, SizeChangeListener)} instead.
     */
    @Deprecated // for removal
    public FormArranger(Composite theParent, Map<Class<?>, EditorFactory<?>> factories, ValueChangeListener listener,
            SizeChangeListener sizeListener) {
        this(theParent, listener, sizeListener);
    }

    /**
     * Echo the change to the second listener after notifying the primary one.
     * 
     * @param primaryListener
     * @param secondListener
     * @return
     */
    public static ValueChangeListener chain(final ValueChangeListener primaryListener,
            final ValueChangeListener secondaryListener) {
        return new ValueChangeListener() {
            @Override
            public void changed(RuleSelection rule, PropertyDescriptor<?> desc, Object newValue) {
                primaryListener.changed(rule, desc, newValue);
                secondaryListener.changed(rule, desc, newValue);
            }

            @Override
            public void changed(PropertySource source, PropertyDescriptor<?> desc, Object newValue) {
                primaryListener.changed(source, desc, newValue);
                secondaryListener.changed(source, desc, newValue);
            }
        };
    }

    protected void register(PropertyDescriptor<?> property, Control[] controls) {
        controlsByProperty.put(property, controls);
    }

    public void clearChildren() {
        Control[] kids = parent.getChildren();
        for (Control kid : kids) {
            kid.dispose();
        }
        parent.pack();
        propertySource = null;
    }

    public int arrangeFor(PropertySource theSource) {
        if (Objects.equals(propertySource, theSource)) {
            return -1;
        }
        return rearrangeFor(theSource);
    }

    public void loadValues() {
        rearrangeFor(propertySource);
    }

    private int rearrangeFor(PropertySource theSource) {
        clearChildren();

        propertySource = theSource;

        if (propertySource == null) {
            return -1;
        }

        Map<PropertyDescriptor<?>, Object> valuesByDescriptor = Configuration.filteredPropertiesOf(propertySource);

        PropertyDescriptor<?>[] orderedDescs = valuesByDescriptor.keySet().toArray(new PropertyDescriptor[0]);
        Arrays.sort(orderedDescs, Comparator.comparing(PropertyDescriptor::name));

        int rowCount = orderedDescs.length; // count up the actual rows with widgets needed, not all have editors yet

        boolean isXPathRule = RuleUtil.isXPathRule(propertySource);
        int columnCount = isXPathRule ? 3 : 2; // xpath descriptors have a column of delete buttons

        GridLayout layout = new GridLayout(columnCount, false);
        layout.verticalSpacing = 2;
        layout.marginTop = 1;
        parent.setLayout(layout);
        widgets = new Control[rowCount][columnCount];



        int rowsAdded = 0;

        for (PropertyDescriptor<?> desc : orderedDescs) {
            if (addRowWidgets(PropertyEditorFactory.INSTANCE, rowsAdded, desc, isXPathRule)) {
                rowsAdded++;
            }
        }

        if (RuleUtil.isXPathRule(propertySource)) {
            addAddButton();
            rowsAdded++;
        }

        if (rowsAdded > 0) {
            parent.pack();
        }

        adjustEnabledStates();

        return rowsAdded;
    }

    private void addAddButton() {
        Button button = new Button(parent, SWT.PUSH);
        button.setText("Add new...");
        button.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                NewPropertyDialog dialog = new NewPropertyDialog(parent.getShell(), propertySource, changeListener);
                if (dialog.open() == Window.OK) {
                    PropertyDescriptor<?> desc = dialog.descriptor();
                    propertySource.definePropertyDescriptor(desc);
                    rearrangeFor(propertySource);
                }
            }
        });
    }

    private boolean addRowWidgets(EditorFactory<?> factory, int rowIndex, PropertyDescriptor desc,
            boolean isXPathRule) {
        if (factory == null) {
            return false;
        }

        // add all the labels & controls necessary on each row
        widgets[rowIndex][0] = factory.addLabel(parent, desc);
        widgets[rowIndex][1] = factory.newEditorOn(parent, desc, propertySource, changeListener, sizeChangeListener);

        if (isXPathRule) {
            widgets[rowIndex][2] = addDeleteButton(parent, desc, propertySource);
        }

        register(desc, widgets[rowIndex]);

        return true;
    }

    private Control addDeleteButton(Composite parent, final PropertyDescriptor<?> desc, final PropertySource source) {

        Button button = new Button(parent, SWT.PUSH);
        button.setData(desc.name()); // for later reference
        button.setImage(ResourceManager.imageFor(PMDUiConstants.ICON_BUTTON_DELETE));

        button.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                // rule.undefine(desc);
                rearrangeFor(source);
                updateDeleteButtons();
                // sizeChangeListener.addedRows(-1); not necessary apres rearrange?
            }
        });

        return button;
    }

    /**
     * Flag the delete buttons linked to property variables that are not referenced in the Xpath source or clear any
     * images they may have. Returns the names of any unreferenced variables are found;
     */
    public List<String> updateDeleteButtons() {
        if (propertySource == null || !RuleUtil.isXPathRule(propertySource)) {
            return Collections.emptyList();
        }

        PropertyDescriptor<String> xpathDescriptor = (PropertyDescriptor<String>) propertySource.getPropertyDescriptor(Configuration.XPATH_EXPRESSION_PROPERTY);
        String source = propertySource.getProperty(xpathDescriptor);
        List<int[]> refPositions = Util.referencedNamePositionsIn(source, '$');
        if (refPositions.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> unreferencedOnes = new ArrayList<>(refPositions.size());
        List<String> varNames = Util.fragmentsWithin(source, refPositions);

        for (Control[] widgetRow : widgets) {
            Button butt = (Button) widgetRow[2];
            String buttonName = (String) butt.getData();
            boolean isReferenced = varNames.contains(buttonName);

            butt.setToolTipText(
                    isReferenced ? "Delete variable: $" + buttonName : "Delete unreferenced variable: $" + buttonName);
            if (!isReferenced) {
                unreferencedOnes.add((String) butt.getData());
            }
        }

        return unreferencedOnes;
    }

    private void adjustEnabledStates() {
        for (Map.Entry<PropertyDescriptor<?>, Control[]> entry : controlsByProperty.entrySet()) {
            SWTUtil.setEnabled(Arrays.asList(entry.getValue()), true);
        }
    }

    @Override
    public void changed(RuleSelection rule, PropertyDescriptor<?> desc, Object newValue) {
        // TODO
    }

    @Override
    public void changed(PropertySource source, PropertyDescriptor<?> desc, Object newValue) {
        adjustEnabledStates();
    }
}
