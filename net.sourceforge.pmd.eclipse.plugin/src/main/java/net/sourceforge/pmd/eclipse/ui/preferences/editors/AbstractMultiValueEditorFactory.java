/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import net.sourceforge.pmd.eclipse.ui.preferences.br.SizeChangeListener;
import net.sourceforge.pmd.eclipse.ui.preferences.br.ValueChangeListener;
import net.sourceforge.pmd.eclipse.util.Util;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertySource;

/**
 * As a stateless factory it is responsible for building editors that manipulating value collections
 * without retaining references to the widgets or values themselves. All necessary references are
 * held in the event handlers and passed onto any new handlers created to manage values newly created
 * by the user - hence the monster method calls with umpteen arguments.
 *
 * <p>Concrete subclasses are responsible for instantiating the type-appropriate edit widgets, retrieving
 * their values, and updating the rule property. Provided you have widget capable of displaying/editing
 * your value type you can use this class as a base to bring up the appropriate widgets for the
 * individual values.
 *
 * <p>The editor is held in a composite divided into three columns. In the collapsed mode, a text field
 * displaying the value collection occupies the first two cells with an expand/collapse button in the
 * last cell. When the user clicks the button the row beneath is given a label, a type-specific edit
 * widget, and a control button for every value in the collection. The last row is empty and serves as
 * a place the user can enter additional values. When the user enters a value and clicks the control
 * button that row becomes read-only and a new empty row is added to the bottom.
 *
 * <p>Note inclusion of the size and value changed callbacks used to let the parent composite resize itself
 * and update the values in the rule listings respectively.
 *
 * @author Brian Remedios
 * @deprecated This editor factory will be removed without replacement. This was only used for supporting the UI
 *             of the plugin and is considered internal API now.
 */
@Deprecated // for removal
public abstract class AbstractMultiValueEditorFactory<T> extends AbstractEditorFactory<List<T>> {

    protected static final String DELIMITER = ",";

    private static final int WIDGETS_PER_ROW = 3;     //  numberLabel, valueWidget, +/-button


    protected AbstractMultiValueEditorFactory() {
        // protected default constructor for subclassing
    }


    protected abstract void configure(Text text, PropertyDescriptor<List<T>> desc, PropertySource source, ValueChangeListener listener);


    protected abstract void setValue(Control widget, T value);


    protected abstract void update(PropertySource source, PropertyDescriptor<List<T>> desc, List<T> newValues);


    protected abstract T addValueIn(Control widget, PropertyDescriptor<List<T>> desc, PropertySource source);


    protected abstract Control addWidget(Composite parent, T value, PropertyDescriptor<List<T>> desc, PropertySource source);


    @Override
    public Control newEditorOn(final Composite parent, final PropertyDescriptor<List<T>> desc,
                               final PropertySource source, final ValueChangeListener changeListener, final SizeChangeListener sizeListener) {

        final Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        panel.setLayout(layout);

        final Text textWidget = new Text(panel, SWT.SINGLE | SWT.BORDER);
        final Button butt = new Button(panel, SWT.PUSH);
        butt.setText("...");    // TODO use triangle icon & rotate 90deg when clicked
        butt.addListener(SWT.Selection, new Listener() {
            boolean itemsVisible = false;
            List<Control> items = new ArrayList<>();

            @Override
            public void handleEvent(Event event) {
                if (itemsVisible) {
                    hideCollection(items);
                    sizeListener.addedRows(items.size() / -WIDGETS_PER_ROW);
                } else {
                    items = openCollection(panel, desc, source, textWidget, changeListener, sizeListener);
                    sizeListener.addedRows(items.size() / WIDGETS_PER_ROW);
                }
                itemsVisible = !itemsVisible;
                textWidget.setEditable(!itemsVisible);   // no raw editing when individual items are available
                parent.layout();
            }
        });
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        textWidget.setLayoutData(data);
        panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        fillWidget(textWidget, desc, source);
        configure(textWidget, desc, source, changeListener);

        return panel;
    }


    private void hideCollection(List<Control> controls) {
        for (Control control : controls) {
            control.dispose();
        }
    }


    private void delete(Control number, Control widget, Control button, List<Control> controlList,
                        T deleteValue, PropertyDescriptor<List<T>> desc, PropertySource source) {

        controlList.remove(number);
        number.dispose();
        controlList.remove(widget);
        widget.dispose();
        controlList.remove(button);
        button.dispose();
        renumberLabelsIn(controlList);

        List<T> values = valueFor(source, desc);
        List<T> newValues = new ArrayList<>(values.size() - 1);
        for (T value : values) {
            if (value.equals(deleteValue)) {
                continue;
            }
            newValues.add(value);
        }

        update(source, desc, newValues);
    }


    private List<Control> openCollection(final Composite parent, final PropertyDescriptor<List<T>> desc,
                                         final PropertySource source, final Text textWidget, final ValueChangeListener changeListener,
                                         final SizeChangeListener sizeListener) {

        final List<Control> newControls = new ArrayList<>();

        int i;
        List<T> values = valueFor(source, desc);
        for (i = 0; i < values.size(); i++) {
            final Label number = new Label(parent, SWT.NONE);
            number.setText(Integer.toString(i + 1));
            final Control widget = addWidget(parent, values.get(i), desc, source);
            widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            widget.setEnabled(false);
            final Button butt = new Button(parent, SWT.PUSH);
            butt.setText("-");  // TODO use icon for consistent width
            final T value = values.get(i);
            butt.addListener(SWT.Selection, new Listener() {  // remove value handler
                @Override
                public void handleEvent(Event event) {
                    delete(number, widget, butt, newControls, value, desc, source);
                    fillWidget(textWidget, desc, source); // j
                    sizeListener.addedRows(-1);
                    changeListener.changed(source, desc, null);
                    parent.getParent().layout();
                }
            });
            newControls.add(number);
            newControls.add(widget);
            newControls.add(butt);
        }

        addNewValueRow(parent, desc, source, textWidget, changeListener, sizeListener, newControls, i);

        return newControls;
    }


    /**
     * Override in subclasses as necessary
     *
     * @param desc
     * @param rule
     *
     * @return
     */
    protected boolean canAddNewRowFor(final PropertyDescriptor<List<T>> desc, final PropertySource source) {
        return true;
    }


    private void addNewValueRow(final Composite parent, final PropertyDescriptor<List<T>> desc,
                                final PropertySource source, final Text parentWidget,
                                final ValueChangeListener changeListener, final SizeChangeListener sizeListener,
                                final List<Control> newControls, int i) {

        if (!canAddNewRowFor(desc, source)) {
            return;
        }

        final Label number = new Label(parent, SWT.NONE);
        number.setText(Integer.toString(i + 1));
        newControls.add(number);
        final Control widget = addWidget(parent, null, desc, source);
        widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        newControls.add(widget);
        final Button butt = new Button(parent, SWT.PUSH);
        butt.setText("+");  // TODO use icon for consistent width
        newControls.add(butt);
        Listener addListener = new Listener() {
            @Override
            public void handleEvent(Event event) {      // add new value handler
                // add the new value to the property set
                // set the value in the widget to the cleaned up one, disable it
                // remove old listener from button, add new (delete) one, update text/icon
                // add new row widgets: label, value widget, button
                // add listener for new button
                T newValue = addValueIn(widget, desc, source);
                if (newValue == null) {
                    return;
                }

                addNewValueRow(parent, desc, source, parentWidget, changeListener, sizeListener, newControls, -1);
                convertToDelete(butt, newValue, parent, newControls, desc, source, parentWidget, number, widget, changeListener, sizeListener);
                widget.setEnabled(false);
                setValue(widget, newValue);

                renumberLabelsIn(newControls);
                fillWidget(parentWidget, desc, source);
                sizeListener.addedRows(1);
                changeListener.changed(source, desc, newValue);
                parent.getParent().layout();
            }
        };
        butt.addListener(SWT.Selection, addListener);
        widget.addListener(SWT.DefaultSelection, addListener);    // allow for CR on entry widgets themselves, no need to click the '+' button
        widget.setFocus();
    }


    private void convertToDelete(final Button button, final T toDeleteValue, final Composite parent,
                                 final List<Control> newControls, final PropertyDescriptor<List<T>> desc, final
                                 PropertySource source, final Text parentWidget, final Label number, final Control widget,
                                 final ValueChangeListener changeListener, final SizeChangeListener sizeListener) {
        button.setText("-");
        Util.removeListeners(button, SWT.Selection);
        button.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                delete(number, widget, button, newControls, toDeleteValue, desc, source);
                fillWidget(parentWidget, desc, source);
                sizeListener.addedRows(-1);
                changeListener.changed(source, desc, null);
                parent.getParent().layout();
            }
        });
    }


    protected void fillWidget(Text textWidget, PropertyDescriptor<List<T>> desc, PropertySource source) {
        List<T> values = valueFor(source, desc);
        textWidget.setText(values == null ? "" : StringUtils.join(values, DELIMITER + ' '));
    }


    protected List<String> textWidgetValues(Text textWidget) {

        String values = textWidget.getText().trim();

        if (StringUtils.isBlank(values)) {
            return Collections.emptyList();
        }

        String[] valueSet = values.split(DELIMITER);
        List<String> valueList = new ArrayList<>(valueSet.length);

        for (String value : valueSet) {
            String str = value.trim();
            if (str.length() > 0) {
                valueList.add(str);
            }
        }

        return valueList;
    }


    private static void renumberLabelsIn(List<Control> controls) {
        int i = 1;
        for (Control control : controls) {
            if (control instanceof Label) {
                ((Label) control).setText(Integer.toString(i++));
            }
        }
    }
}
