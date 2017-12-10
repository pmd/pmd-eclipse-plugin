# PMD Eclipse Plugin - Test Projects

## Getting started

1.  Download vanilla eclipse from http://www.eclipse.org/
    The package is "Eclipse IDE for Java Developers"

2.  Install pmd-eclipse-plugin from http://dl.bintray.com/pmd/pmd-eclipse-plugin/updates/

3.  Install m2e-code-quality plugin from http://m2e-code-quality.github.io/m2e-code-quality/site/

4.  Just open the workspace with your fresh eclipse.

## Test projects overview

*   workspace1: Simple java project
    Global Rule Management is enabled, all rules are activated.
    PMD is enabled for the project1. See the source file
    `TestViolation.java` for the expected violations.

*   workspace2: Another simple java project
    Global Rule Management is enabled, all rules are activated.
    PMD is enabled for the project, the option "Use the ruleset configured
    in a project file" is used. The project ruleset file has been created by
    pmd-eclipse-plugin itself. See `.ruleset`.

*   workspace3: Another simple java project
    Global Rule Management is enabled, all rules are activated.
    PMD is enabled for the project, the option "Use the ruleset configured
    in a project file" is used. The project ruleset file has been
    created manually. See `ruleset.xml`.

*   workspace4: a maven java project with maven-pmd-plugin activated
    Global Rule Management is enabled, all rules are activated.
    The pmd plugin should be configured via m2e-code-quality

*   workspace5: a general project called `apexproject1`. It contains
    one sample apex file which triggered a rule violation.
