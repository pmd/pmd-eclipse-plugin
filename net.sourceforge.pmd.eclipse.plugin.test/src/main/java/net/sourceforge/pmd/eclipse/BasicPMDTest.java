/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.document.FileId;
import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.lang.rule.RuleSetLoader;
import net.sourceforge.pmd.reporting.Report;
import net.sourceforge.pmd.reporting.RuleViolation;

/**
 * Test if PMD can be run correctly.
 * 
 * @author Philippe Herlin
 */
public class BasicPMDTest {
    /**
     * Try to load all the plugin known rulesets.
     * 
     */
    @Test
    public void testDefaulltRuleSets() {
        RuleSetLoader rulesetloader = new RuleSetLoader();
        List<RuleSet> standardRuleSets = rulesetloader.getStandardRuleSets();
        Assert.assertFalse("No Rulesets found", standardRuleSets.isEmpty());
    }

    private void runPmd() {
        PMDConfiguration configuration = new PMDConfiguration();
        configuration.setRuleSets(Arrays.asList("category/java/codestyle.xml/UnnecessaryReturn"));
        configuration.setIgnoreIncrementalAnalysis(true);

        final String sourceCode = "public class Foo {\n public void foo() {\nreturn;\n}}";

        try (PmdAnalysis pmd = PmdAnalysis.create(configuration)) {
            pmd.files().addSourceFile(FileId.fromPathLikeString("Foo.java"), sourceCode);
            Report result = pmd.performAnalysisAndCollectReport();

            Assert.assertFalse("There should be at least one violation", result.getViolations().isEmpty());

            final RuleViolation violation = result.getViolations().get(0);
            Assert.assertEquals(violation.getRule().getName(), "UnnecessaryReturn");
            Assert.assertEquals(3, violation.getBeginLine());
        }
        
    }

    /**
     * One first thing the plugin must be able to do is to run PMD.
     * 
     */
    @Test
    public void testRunPmd() {
        runPmd();
    }
}
