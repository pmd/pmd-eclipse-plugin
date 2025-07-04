/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.writer;

/**
 * General exception thrown by objects of this package
 *
 * @author Herlin
 *
 */

public class WriterException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public WriterException() {
        super();
    }

    /**
     * @param arg0
     * @param arg1
     */
    public WriterException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public WriterException(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public WriterException(Throwable arg0) {
        super(arg0);
    }

}
