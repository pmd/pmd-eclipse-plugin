/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.eclipse.ui.actions.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class InternalRuleSetUtilTest {

    @Test
    public void convertStringPatterns() {
        Set<String> stringPatterns = new HashSet<>();
        stringPatterns.add(".*src/main/java.*");
        
        Collection<Pattern> patterns = InternalRuleSetUtil.convertStringPatterns(stringPatterns);
        Assert.assertEquals(1, patterns.size());
        Assert.assertEquals(stringPatterns.toString(), patterns.toString());
    }
}
