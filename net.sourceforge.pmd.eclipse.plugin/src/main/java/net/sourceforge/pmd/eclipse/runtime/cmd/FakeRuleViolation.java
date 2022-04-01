/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.runtime.cmd;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleViolation;

/**
 * This is an implementation of IRuleViolation. It is meant to rebuild a
 * RuleViolation from a PMD Marker. This object is used to generate violation
 * reports.
 * 
 * @author Herlin
 * @author Brian Remedios
 */

class FakeRuleViolation implements RuleViolation {
    private int beginLine;
    private int beginColumn;
    private int endLine;
    private int endColumn;

    private String filename;
    private String packageName;
    private String className;
    private String methodName;
    private String variableName;

    private Rule rule;

    private String description = "";

    /**
     * Default constructor take a rule object to initialize. All other variables
     * have default values to empty;
     * 
     * @param rule
     */
    FakeRuleViolation(Rule theRule) {
        this.rule = theRule;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * @param beginColumn
     *            The beginColumn to set.
     */
    public void setBeginColumn(int beginColumn) {
        this.beginColumn = beginColumn;
    }

    /**
     * @param beginLine
     *            The beginLine to set.
     */
    public void setBeginLine(int beginLine) {
        this.beginLine = beginLine;
    }

    /**
     * @param className
     *            The className to set.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param endColumn
     *            The endColumn to set.
     */
    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

    /**
     * @param endLine
     *            The endLine to set.
     */
    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    /**
     * @param filename
     *            The filename to set.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @param methodName
     *            The methodName to set.
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * @param packageName
     *            The packageName to set.
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * @param variableName
     *            The variableName to set.
     */
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public boolean isSuppressed() {
        return false;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public int getBeginLine() {
        return beginLine;
    }

    @Override
    public int getBeginColumn() {
        return beginColumn;
    }

    @Override
    public int getEndLine() {
        return endLine;
    }

    @Override
    public int getEndColumn() {
        return endColumn;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String getVariableName() {
        return variableName;
    }
}
