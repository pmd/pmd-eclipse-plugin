/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PMDException;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.SourceCodeProcessor;
import net.sourceforge.pmd.lang.LanguageRegistry;
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
    }

    /**
     * Try to load all the plugin known rulesets
     * 
     */
    @Test
    public void testDefaulltRuleSets() {
        try {
            final RuleSetFactory factory = new RuleSetFactory();
            final Iterator<RuleSet> iterator = factory.getRegisteredRuleSets();
            while (iterator.hasNext()) {
                iterator.next();
            }
        } catch (final RuleSetNotFoundException e) {
            e.printStackTrace();
            Assert.fail("unable to load registered rulesets ");
        }
    }

    /**
     * One first thing the plugin must be able to do is to run PMD
     * 
     */
    @Test
    public void testRunPmdJdk13() {

        try {
            PMDConfiguration configuration = new PMDConfiguration();
            configuration.setDefaultLanguageVersion(LanguageRegistry.findLanguageVersionByTerseName("java 1.3"));

            final String sourceCode = "public class Foo {\n public void foo() {\nreturn;\n}}";

            final RuleContext context = new RuleContext();
            context.setSourceCodeFilename("foo.java");
            context.setReport(new Report());

            final RuleSet codeStyleRuleSet = new RuleSetFactory().createRuleSet("category/java/codestyle.xml/UnnecessaryReturn");
            RuleSets rSets = new RuleSets(codeStyleRuleSet);
            new SourceCodeProcessor(configuration).processSourceCode(new StringDataSource(sourceCode).getInputStream(),
                    rSets, context);

            final Iterator<RuleViolation> iter = context.getReport().iterator();
            Assert.assertTrue("There should be at least one violation", iter.hasNext());

            final RuleViolation violation = iter.next();
            Assert.assertEquals(violation.getRule().getName(), "UnnecessaryReturn");
            Assert.assertEquals(3, violation.getBeginLine());

        } catch (final RuleSetNotFoundException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (final PMDException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * Let see with Java 1.4
     * 
     */
    @Test
    public void testRunPmdJdk14() {

        try {
            PMDConfiguration configuration = new PMDConfiguration();
            configuration.setDefaultLanguageVersion(LanguageRegistry.findLanguageVersionByTerseName("java 1.4"));

            final String sourceCode = "public class Foo {\n public void foo() {\nreturn;\n}}";

            final RuleContext context = new RuleContext();
            context.setSourceCodeFilename("foo.java");
            context.setReport(new Report());

            final RuleSet codeStyleRuleSet = new RuleSetFactory().createRuleSet("category/java/codestyle.xml/UnnecessaryReturn");
            RuleSets rSets = new RuleSets(codeStyleRuleSet);
            new SourceCodeProcessor(configuration).processSourceCode(new StringDataSource(sourceCode).getInputStream(),
                    rSets, context);

            final Iterator<RuleViolation> iter = context.getReport().iterator();
            Assert.assertTrue("There should be at least one violation", iter.hasNext());

            final RuleViolation violation = iter.next();
            Assert.assertEquals(violation.getRule().getName(), "UnnecessaryReturn");
            Assert.assertEquals(3, violation.getBeginLine());

        } catch (final RuleSetNotFoundException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (final PMDException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * Let see with Java 1.5
     * 
     */
    @Test
    public void testRunPmdJdk15() {

        try {
            PMDConfiguration configuration = new PMDConfiguration();
            configuration.setDefaultLanguageVersion(LanguageRegistry.findLanguageVersionByTerseName("java 1.5"));

            final String sourceCode = "public class Foo {\n public void foo() {\nreturn;\n}}";

            final RuleContext context = new RuleContext();
            context.setSourceCodeFilename("foo.java");
            context.setReport(new Report());

            final RuleSet codeStyleRuleSet = new RuleSetFactory().createRuleSet("category/java/codestyle.xml/UnnecessaryReturn");
            RuleSets rSets = new RuleSets(codeStyleRuleSet);
            new SourceCodeProcessor(configuration).processSourceCode(new StringDataSource(sourceCode).getInputStream(),
                    rSets, context);

            final Iterator<RuleViolation> iter = context.getReport().iterator();
            Assert.assertTrue("There should be at least one violation", iter.hasNext());

            final RuleViolation violation = iter.next();
            Assert.assertEquals(violation.getRule().getName(), "UnnecessaryReturn");
            Assert.assertEquals(3, violation.getBeginLine());

        } catch (final RuleSetNotFoundException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (final PMDException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
