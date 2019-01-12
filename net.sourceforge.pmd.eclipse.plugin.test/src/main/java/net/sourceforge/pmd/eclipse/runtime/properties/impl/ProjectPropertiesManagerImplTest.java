/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.properties.impl;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class ProjectPropertiesManagerImplTest {

    private ProjectPropertiesManagerImpl manager = new ProjectPropertiesManagerImpl();

    @Test
    public void testToString() throws Exception {
        ProjectPropertiesTO projectProperties = createProjectProperties();
        String expected = IOUtils.toString(this.getClass().getResourceAsStream("projectproperties.xml"));
        expected = expected.replaceAll("\r\n", "\n");

        String s = manager.convertProjectPropertiesToString(projectProperties);
        Assert.assertEquals(expected, s);
    }

    @Test
    public void testFromString() throws Exception {
        String input = IOUtils.toString(this.getClass().getResourceAsStream("projectproperties.xml"));
        ProjectPropertiesTO projectProperties = manager.convertProjectPropertiesFromString(input);
        ProjectPropertiesTO expected = createProjectProperties();

        Assert.assertArrayEquals(expected.getExcludePatterns(), projectProperties.getExcludePatterns());
        Assert.assertEquals(expected.isFullBuildEnabled(), projectProperties.isFullBuildEnabled());
        Assert.assertEquals(expected.isIncludeDerivedFiles(), projectProperties.isIncludeDerivedFiles());
        Assert.assertArrayEquals(expected.getIncludePatterns(), projectProperties.getIncludePatterns());
        Assert.assertEquals(expected.getRuleSetFile(), projectProperties.getRuleSetFile());
        Assert.assertEquals(expected.isRuleSetStoredInProject(), projectProperties.isRuleSetStoredInProject());
        Assert.assertEquals(expected.isViolationsAsErrors(), projectProperties.isViolationsAsErrors());
        Assert.assertEquals(expected.getWorkingSetName(), projectProperties.getWorkingSetName());

        Assert.assertEquals(expected.getRules().length, projectProperties.getRules().length);
        for (int i = 0; i < expected.getRules().length; i++) {
            Assert.assertEquals(expected.getRules()[i].getName(), projectProperties.getRules()[i].getName());
            Assert.assertEquals(expected.getRules()[i].getRuleSetName(),
                    projectProperties.getRules()[i].getRuleSetName());
        }
    }

    private ProjectPropertiesTO createProjectProperties() {
        ProjectPropertiesTO projectProperties = new ProjectPropertiesTO();
        projectProperties.setExcludePatterns(new String[] { ".project" });
        projectProperties.setFullBuildEnabled(true);
        projectProperties.setIncludeDerivedFiles(false);
        projectProperties.setIncludePatterns(new String[] { "*.java" });
        projectProperties.setRuleSetFile("rulesetfile");
        projectProperties.setRuleSetStoredInProject(true);
        projectProperties.setViolationsAsErrors(false);
        projectProperties.setWorkingSetName("workingsetname");
        projectProperties.setRules(new RuleSpecTO[] { createRule("JumbledIncrementer", "java-basic"),
            createRule("ForLoopShouldBeWhileLoop", "java-basic") });
        return projectProperties;
    }

    private RuleSpecTO createRule(String name, String rulesetname) {
        RuleSpecTO rulespec = new RuleSpecTO();
        rulespec.setName(name);
        rulespec.setRuleSetName(rulesetname);
        return rulespec;
    }
}
