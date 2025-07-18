/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * A custom control intended to display and accept Type values. New values are validated when the widget loses focus, if
 * the text represents a recognized class then it is re-rendered with its full package name. If it isn't recognized or
 * is a disallowed primitive then the entry is cleared.
 *
 * <p>TODO - add a grey prompt within the field when it is empty, remove when user starts typing
 *
 * @author Brian Remedios
 * @deprecated This class is not used anymore and will be removed.
 */
@Deprecated // for removal
public class TypeText extends Composite {

    private Text text;
    private boolean acceptPrimitives;

    public TypeText(Composite parent, int style, boolean primitivesOK, String thePromptText) { // NOPMD: thePromptText is unused TODO
        super(parent, SWT.None);

        GridLayout layout = new GridLayout(1, false);
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);

        text = new Text(this, style);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                // adjust to remove prompt text if necessary
                // TODO
            }
        });

        acceptPrimitives = primitivesOK;
    }

    @Override
    public void addListener(int eventType, Listener listener) {
        super.addListener(eventType, listener);
        if (text == null) {
            return;
        }
        text.addListener(eventType, listener);
    }

    @Override
    public void removeListener(int eventType, Listener listener) {
        text.removeListener(eventType, listener);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return text.computeSize(wHint, hHint, changed);
    }

    public void setType(Class<?> cls) {
        if (cls == null) {
            text.setText("");
            return;
        }

        if (cls.isPrimitive() && !acceptPrimitives) {
            setType(null);
            return;
        }

        text.setText(cls.getName());
    }

    @Override
    public void setBackground(Color clr) {
        text.setBackground(clr);
    }

    @Override
    public void setEnabled(boolean flag) {
        super.setEnabled(flag);
        text.setEnabled(flag);
    }

    public void setEditable(boolean flag) {
        text.setEditable(flag);
    }

    public Class<?> getType(boolean doCleanup) {

        String typeStr = text.getText().trim();
        if (StringUtils.isBlank(typeStr)) {
            if (doCleanup) {
                setType(null);
            }
            return null;
        }

        Class<?> cls = null;
        try {
            cls = TypeText.class.getClassLoader().loadClass(typeStr);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (cls != null && cls.isPrimitive() && !acceptPrimitives) {
            cls = null;
        }

        if (cls != null) {
            if (doCleanup) {
                setType(cls);
            }
            return cls;
        }

        // FIXME - incorporate this
        // IJavaProject project = getJavaProject();
        // IStatus status = JavaConventions.validateClassFileName(typeStr, project.getOption(JavaCore.COMPILER_SOURCE,
        // true), project.getOption(JavaCore.COMPILER_COMPLIANCE, true));

        try {
            return Class.forName(typeStr);
        } catch (Exception ex) {
            if (doCleanup) {
                setType(null);
            }
            return null;
        }
    }
}
