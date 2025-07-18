/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A general purpose selection widget that deals with files.
 *
 * @author Brian Remedios
 * @deprecated This class is not used and will be removed.
 */
@Deprecated // for removal
public class FilePicker extends Composite {

    private Text fileField;
    private Button pickButton;
    private final String dialogTitle;
    private final String[] filterExtensions;

    public FilePicker(final Composite parent, int style, String theDialogTitle, String[] theFilterExtensions) {
        super(parent, SWT.None);

        dialogTitle = theDialogTitle;
        filterExtensions = theFilterExtensions;

        GridLayout layout = new GridLayout(3, true);
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);

        fileField = new Text(this, style);
        fileField.addListener(SWT.FocusOut, new Listener() {
            @Override
            public void handleEvent(Event event) {
                // reviseMethodListFor(fileField.getText());
            }
        });

        fileField.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                // reviseMethodListFor(fileField.getText()); // no cleanup,
                // avoid event loop & overflow
            }
        });

        pickButton = new Button(this, SWT.PUSH);
        pickButton.setText("...");
        pickButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
        pickButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                openFileDialog(parent.getShell());
            }
        });

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        fileField.setLayoutData(data);
    }

    public void addFocusOutListener(Listener listener) {
        fileField.addListener(SWT.FocusOut, listener);
    }

    private void openFileDialog(Shell shell) {

        FileDialog fd = new FileDialog(shell, SWT.OPEN);
        fd.setText(dialogTitle);
        fd.setFilterPath("C:/");

        if (filterExtensions != null) {
            fd.setFilterExtensions(filterExtensions);
        }

        String selected = fd.open();

        fileField.setText(selected == null ? "" : selected);
        fileField.setFocus();
    }

    @Override
    public void setBackground(Color clr) {
        fileField.setBackground(clr);
    }

    public void setFile(File file) {
        fileField.setText(file == null ? "" : file.getAbsolutePath());
    }

    @Override
    public void setEnabled(boolean flag) {
        super.setEnabled(flag);
        fileField.setEnabled(flag);
        pickButton.setEnabled(flag);
    }

    public void setEditable(boolean flag) {
        fileField.setEditable(flag);
    }

    public File getFile() {
        String name = fileField.getText();
        return StringUtils.isBlank(name) ? null : new File(name);
    }
}
