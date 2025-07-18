/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.properties;

/**
 * This exception can be thrown by operations on properties
 *
 * @author Herlin
 *
 */

public class PropertiesException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public PropertiesException() {
        super();
    }

    /**
     * @param arg0
     * @param arg1
     */
    public PropertiesException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public PropertiesException(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public PropertiesException(Throwable arg0) {
        super(arg0);
    }

}
