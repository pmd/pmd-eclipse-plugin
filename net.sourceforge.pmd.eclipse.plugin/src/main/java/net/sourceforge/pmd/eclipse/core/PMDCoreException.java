/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.core;

/**
 * Root exception of the CORE plug-in
 *
 * @author Herlin
 *
 */

public class PMDCoreException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public PMDCoreException() {
        super();
    }

    /**
     * Constructor with a message and a root cause.
     * @param arg0 exception message.
     * @param arg1 root cause exception.
     */
    public PMDCoreException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Constructor with only a message.
     * @param arg0 exception message.
     */
    public PMDCoreException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor with a root cause exception only
     * @param arg0 root cause exception
     */
    public PMDCoreException(Throwable arg0) {
        super(arg0);
    }

}
