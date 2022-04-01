/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.util.internal;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {

    @Test
    public void testMaxCommonLeadingWhitespaceForAll() {
        Assert.assertEquals(4, StringUtil.maxCommonLeadingWhitespaceForAll(new String[] { "    a", "    b" }));
        Assert.assertEquals(0, StringUtil.maxCommonLeadingWhitespaceForAll(new String[] { "    a", "b" }));
        Assert.assertEquals(4, StringUtil.maxCommonLeadingWhitespaceForAll(new String[] { "    a", "        b" }));
    }

    @Test
    public void testTrimStartOn() {
        Assert.assertArrayEquals(new String[] { "a", "b" },
                StringUtil.trimStartOn(new String[] { "    a", "    b" }, 4));
        Assert.assertArrayEquals(new String[] { "a", "  b" },
                StringUtil.trimStartOn(new String[] { "  a", "    b" }, 2));
        Assert.assertArrayEquals(new String[] { "    a", "    b" },
                StringUtil.trimStartOn(new String[] { "    a", "    b" }, 0));
    }
}
