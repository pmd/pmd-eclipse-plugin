/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui;

/**
 * 
 * @author Brian Remedios
 */
public interface ColumnDescriptor {

    String id();

    String label();

    String tooltip();

    int defaultWidth();
}
