/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetLoader;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.util.datasource.DataSource;

/**
 * Test if PMD can be run correctly
 * 
 * @author Philippe Herlin
 * 
 */
public class BasicPMDTest {

    static class StringDataSource implements DataSource {
        private final ByteArrayInputStream is;

        StringDataSource(final String source) {
            this.is = new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public InputStream getInputStream() {
            return is;
        }

        @Override
        public String getNiceFileName(final boolean shortNames, final String inputFileName) {
            return "somefile.txt";
        }

        @Override
        public void close() throws IOException {
            // no-op
        }
    }

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

    private void runPmd(String javaVersion) {
        PMDConfiguration configuration = new PMDConfiguration();
        configuration.setDefaultLanguageVersion(
                LanguageRegistry.PMD.getLanguageById(JavaLanguageModule.TERSE_NAME).getVersion(javaVersion));
        configuration.setRuleSets(Arrays.asList("category/java/codestyle.xml/UnnecessaryReturn"));
        configuration.setIgnoreIncrementalAnalysis(true);

        final String sourceCode = "public class Foo {\n public void foo() {\nreturn;\n}}";

        try (PmdAnalysis pmd = PmdAnalysis.create(configuration)) {
            pmd.files().addSourceFile("Foo.java", sourceCode);
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
    public void testRunPmdJdk13() {
        runPmd("1.3");
    }

    /**
     * Let see with Java 1.4.
     * 
     */
    @Test
    public void testRunPmdJdk14() {
        runPmd("1.4");
    }

    /**
     * Let see with Java 1.5.
     * 
     */
    @Test
    public void testRunPmdJdk15() {
        runPmd("1.5");
    }
}
