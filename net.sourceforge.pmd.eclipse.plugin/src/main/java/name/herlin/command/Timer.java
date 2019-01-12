/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package name.herlin.command;

/**
 * This class lets measure time
 * 
 * @author Herlin
 *
 * @deprecated This class will be removed with the next major version of the PMD Plugin.
 */
@Deprecated
public class Timer {
    private long topStart;
    private long topStop;
    
    /**
     * Default constructor that also starts the timer
     *
     */
    public Timer() {
        super();
        topStart = System.currentTimeMillis();
    }
    
    /**
     * Stop the timer
     *
     */
    public void stop() {
        topStop = System.currentTimeMillis();
    }
    
    /**
     * @return measured interval in milliseconds
     */
    public long getDuration() {
        return topStop - topStart;
    }

}
