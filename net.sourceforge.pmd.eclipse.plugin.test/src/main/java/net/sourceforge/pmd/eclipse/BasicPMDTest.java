/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.RulesetsFactoryUtils;
import net.sourceforge.pmd.ThreadSafeReportListener;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.stat.Metric;
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
            try {
                this.is = new ByteArrayInputStream(source.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
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
        try {
            final RuleSetFactory factory = RulesetsFactoryUtils.defaultFactory();
            final Iterator<RuleSet> iterator = factory.getRegisteredRuleSets();
            while (iterator.hasNext()) {
                iterator.next();
            }
        } catch (final RuleSetNotFoundException e) {
            e.printStackTrace();
            Assert.fail("unable to load registered rulesets ");
        }
    }

    private void runPmd(String javaVersion) {
        PMDConfiguration configuration = new PMDConfiguration();
        configuration.setDefaultLanguageVersion(LanguageRegistry.findLanguageByTerseName("java").getVersion(javaVersion));
        configuration.setRuleSets("category/java/codestyle.xml/UnnecessaryReturn");
        RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.createFactory(configuration);

        List<DataSource> files = new ArrayList<>();
        final String sourceCode = "public class Foo {\n public void foo() {\nreturn;\n}}";
        files.add(new StringDataSource(sourceCode));

        final List<RuleViolation> violations = new ArrayList<>();
        final RuleContext ctx = new RuleContext();
        ctx.setSourceCodeFile(new File("foo.java"));
        ctx.getReport().addListener(new ThreadSafeReportListener() {
            @Override
            public void ruleViolationAdded(RuleViolation ruleViolation) {
                violations.add(ruleViolation);
            }

            @Override
            public void metricAdded(Metric metric) {
            }
        });


        PMD.processFiles(configuration, ruleSetFactory, files, ctx,
                Collections.<Renderer>emptyList());

        Assert.assertFalse("There should be at least one violation", violations.isEmpty());

        final RuleViolation violation = violations.get(0);
        Assert.assertEquals(violation.getRule().getName(), "UnnecessaryReturn");
        Assert.assertEquals(3, violation.getBeginLine());
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
