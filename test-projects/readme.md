# PMD Eclipse Plugin - Test Projects

## Getting started

1.  Download vanilla eclipse from http://www.eclipse.org/
    The package is "Eclipse IDE for Java Developers"

2.  Install pmd-eclipse-plugin from http://dl.bintray.com/pmd/pmd-eclipse-plugin/updates/

3.  Install m2e-code-quality plugin from http://m2e-code-quality.github.io/m2e-code-quality/site/

4.  Create a new workspace

5.  Import the projects as described.

## Test projects overview

*   poject1: Simple java project - import as "Existing Projects into Workspace".
    PMD is enabled for the project1. See the source file
    `TestViolation.java` for the expected violations.

*   project2: Another simple java project - import as "Existing Projects into Workspace".
    PMD is enabled for the project, the option "Use the ruleset configured
    in a project file" is used. The project ruleset file has been
    created manually. See `ruleset.xml`.

*   project3: a maven java project with maven-pmd-plugin activated - import as "Existing Maven Projects".
    The pmd plugin should be configured automatically via m2e-code-quality.

*   project4: a maven java project with multiple rulesets - import as "Existing Maven Projects".
    The pmd plugin should be configured automatically via m2e-code-quality.
    Known issue: The rules of the two rulesets are combined but not the excludes.

*   project5: a general project called `project5`. It contains
    one sample apex file which triggers a rule violation.

*   project-with-libs: A couple of eclipse-only projects (no maven), which depend on each other
    or on jar files - import as "Existing Projects into Workspace". There are 4 projects:
    *   project6 - the main project
    *   sample-lib1 - lib project, that produces a jar. Use "create-jar.jardesc" to create
        the jar "sample-lib1-v1.jar" in the same project folder.
    *   sample-lib2 - lib project, that is directly part of project6's classpath. There is nothing
        special to do after import.
    *   sample-lib3 - lib project, that produces a jar at the absolute location `/tmp/sample-lib3-v1.jar`.
        Use "create-jar.jardesc" to create the jar in "/tmp/sample-lib3-v1.jar"
    *   sample-lib4 - lib project, that produces a jar, that is stored in folder "lib" in project6.
        This lib is on the classpath of project6. Use "create-jar.jardesc" to create the jar.
    *   then execute PMD on project6. It should create a "MissingOverride" violation in
        each of the classes "Sample1", "Sample2", "Sample3", and "Sample4".
