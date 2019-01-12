/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import net.sourceforge.pmd.eclipse.plugin.UISettings;

public class ShapeSetCanvasTest {
    private ShapeSetCanvasTest() {}

    public static void main(String[] args) {
        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setLayout(new GridLayout());

        ShapePicker<Shape> ssc = new ShapePicker<Shape>(shell, SWT.None, 36);
        ssc.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        ssc.setSize(770, 55);

        ssc.setShapeMap(UISettings.shapeSet(new RGB(255, 255, 255), 15));
        ssc.setItems(UISettings.allShapes());
        shell.pack();

        shell.setBounds(10, 10, 780, 200);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
