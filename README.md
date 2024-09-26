# PMD Eclipse Plugin

[![Build Status](https://github.com/pmd/pmd-eclipse-plugin/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/pmd/pmd-eclipse-plugin/actions/workflows/build.yml)
[![Eclipse Marketplace](https://img.shields.io/eclipse-marketplace/v/pmd-eclipse-plugin.svg)](https://marketplace.eclipse.org/content/pmd-eclipse-plugin)

Release Notes: <https://github.com/pmd/pmd-eclipse-plugin/blob/main/ReleaseNotes.md>

Eclipse Update Site:

*   Releases: <https://pmd.github.io/pmd-eclipse-plugin-p2-site/>
*   Snapshots: <https://pmd.github.io/pmd-eclipse-plugin-p2-site/snapshot/>

Marketplace: [![Drag to your running Eclipse workspace. Requires Eclipse Marketplace Client](https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.png)](http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=2755329)

## How to contribute

You can contribute by testing the latest version, creating bug reports, or even forking
the repository on github and create pull requests. Any contributions are welcome!


### Testing the latest version

The plugin requires Java 17 for building.

Then simply build the plugin locally using maven:

    ./mvnw clean verify

You'll find the zipped update site in the folder `net.sourceforge.pmd.eclipse.p2updatesite/target/`.
Point eclipse to the zip file in this folder as an update-site and install the
latest SNAPSHOT version.


### Bug Reports

Please file any bug reports in the bug tracker at github:

<https://github.com/pmd/pmd-eclipse-plugin/issues>

### GitHub Repository

Just fork the the GitHub Repository pmd/pmd-eclipse-plugin and create a pull request.

<https://github.com/pmd/pmd-eclipse-plugin/>

To get started, see also the next section.


## Short Developer's Guide

### Compilation
Simply run `./mvn clean verify`. The plugin's update site will be generated in
`net.sourceforge.pmd.eclipse.p2updatesite/target/repository`. You can use this directory as
an update site to install the new plugin version directly into your Eclipse.

### Importing the projects in Eclipse
Make sure you have the Maven Integration (m2e - http://eclipse.org/m2e/) installed. Then you can
import *Existing Maven Projects*.
You should see 6 projects:

* net.sourceforge.pmd.eclipse - that's the feature
* net.sourceforge.pmd.eclipse.p2updatesite - generates the update site
* net.sourceforge.pmd.eclipse.parent - the parent pom project
* net.sourceforge.pmd.eclipse.plugin - the actual plugin code
* net.sourceforge.pmd.eclipse.plugin.test - the (unit) tests for the plugin
* net.sourceforge.pmd.eclipse.plugin.test.fragment - an example extension of the plugin used during the tests

### Debugging
You can run eclipse with debugging enabled and connect to it via remote debugging:

    eclipse -data workspace-directory -vmargs -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000


### Releasing and updating the official eclipse update site

The update site is hosted on github as a Github Pages site of the repository
<https://github.com/pmd/pmd-eclipse-plugin-p2-site/>.

The release script running on Github Actions will automatically update the repository pmd-eclipse-plugin-p2-site and
add the new release, update the repository metadata (compositeContent.xml and compositeArtifacts.xml
as well as index.md) and push the changes.

The release happens in two phases:

1.  Update the repository locally to prepare the new version:
    *   Update the changelog
    *   Update the versions
    *   Create a tag
    *   Update the changelog for the next version
    *   Update the versions
2.  Push the changes and the tag. The [Github Actions build](https://github.com/pmd/pmd-eclipse-plugin/actions) will
    then publish the new version on [update site](https://github.com/pmd/pmd-eclipse-plugin-p2-site/) and
    [github releases](https://github.com/pmd/pmd-eclipse-plugin/releases). It will also upload the update site
    to [sourceforge](https://sourceforge.net/projects/pmd/files/pmd-eclipse/zipped/) and create a sourceforge
    [blog entry](https://sourceforge.net/p/pmd/news/).


#### Script

See `do-release.sh`.

Verify, that the zipped update site has been uploaded to
[GitHub Releases](https://github.com/pmd/pmd-eclipse-plugin/releases) and [sourceforge](https://pmd.github.io/pmd-eclipse-plugin-p2-site/)
and that a [news blog entry](https://sourceforge.net/p/pmd/news/) has been created on sourceforge.

### Updating the used PMD version
The parent pom contains the property `pmd.version`. This is used inside the plugin module, to resolve the dependencies.
In order to change the PMD version, change this property and rebuild (`mvn clean package`). In case PMD has some
changed (added/removed) transitive dependencies, you'll need to update `n.s.p.e.plugin/META-INF/MANIEFEST.MF` as well.
All transitive dependencies are copied into the folder `n.s.p.e.plugin/target/lib` during the build.


## Useful References

* <http://wiki.eclipse.org/Equinox/p2/Publisher>
* <http://wiki.eclipse.org/Equinox_p2_Repository_Mirroring>
* <http://wiki.eclipse.org/Category:Tycho>
* <http://wiki.eclipse.org/Tycho/Additional_Tools>
* <http://codeiseasy.wordpress.com/2012/07/26/managing-a-p2-release-repository-with-tycho/>
* <http://wiki.eclipse.org/Tycho/Demo_Projects>
* <http://wiki.eclipse.org/Tycho/Reference_Card>
* <http://eclipse.org/tycho/sitedocs/index.html>
* <https://docs.sonatype.org/display/M2ECLIPSE/Staging+and+releasing+new+M2Eclipse+release>
* <http://wiki.eclipse.org/Tycho/Packaging_Types>
* <http://wiki.eclipse.org/Tycho/Reproducible_Version_Qualifiers>
* <http://www.vogella.com/articles/EclipseTycho/article.html>
* <http://git.eclipse.org/c/tycho/org.eclipse.tycho-demo.git/tree/itp01/tycho.demo.itp01.tests/pom.xml>
* <http://www.sonatype.com/people/2008/11/building-eclipse-plugins-with-maven-tycho/>
* <http://zeroturnaround.com/labs/building-eclipse-plug-ins-with-maven-3-and-tycho/>
* <https://github.com/open-archetypes/tycho-eclipse-plugin-archetype>
* <http://wiki.eclipse.org/Tycho/How_Tos/Dependency_on_pom-first_artifacts>
