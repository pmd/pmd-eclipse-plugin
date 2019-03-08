# PMD Eclipse Plugin

[![Build Status](https://travis-ci.org/pmd/pmd-eclipse-plugin.svg?branch=master)](https://travis-ci.org/pmd/pmd-eclipse-plugin)
[![Eclipse Marketplace](https://img.shields.io/eclipse-marketplace/v/pmd-eclipse-plugin.svg)](https://marketplace.eclipse.org/content/pmd-eclipse-plugin)

Release Notes: <https://github.com/pmd/pmd-eclipse-plugin/blob/master/ReleaseNotes.md>

Eclipse Update Site:

*   Releases: <https://dl.bintray.com/pmd/pmd-eclipse-plugin/updates/>
*   Snapshots: <https://dl.bintray.com/pmd/pmd-eclipse-plugin/snapshots/updates/>

Marketplace: [![Drag to your running Eclipse workspace. Requires Eclipse Marketplace Client](https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.png)](http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=2755329)

## How to contribute

You can contribute by testing the latest version, creating bug reports, or even forking
the repository on github and create pull requests. Any contributions are welcome!


### Testing the latest version
Simply build the plugin locally using maven:

    ./mvnw clean verify

You'll find the zipped update site in the folder `net.sourceforge.pmd.eclipse.p2updatesite/target/`. Point eclipse to the zip file in this folder as an update-site and install the
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

    eclipse -data workspace-directory -vmargs -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000


### Releasing and updating the official eclipse update site

From now on, we use [bintray](https://bintray.com) for hosting the eclipse update site.
There is a nice [blog post by Lorenzo Bettini](http://www.lorenzobettini.it/2016/02/publish-an-eclipse-p2-composite-repository-on-bintray/), which explains how it is done. There is also an [example repository](https://github.com/LorenzoBettini/p2composite-bintray-example) on github.


Have a look at the `net.sourceforge.pmd.eclipse.p2updatesite` module, there you see

*   a profile `release-composite` which enables the steps
*   the ant script `bintray.ant` which is used to upload and download the site
*   the ant script `packaging-p2composite.ant` which is used to modify the metadata of the
    p2 repo locally before uploading

The release happens in two phases:

1.  Update the repository locally to prepare the new version:
    *   Update the changelog
    *   Update the versions
    *   Create a tag
    *   Update the changelog for the next version
    *   Update the versions
2.  Push the changes and the tag. The [travis build](https://travis-ci.org/pmd/pmd-eclipse-plugin) will
    then publish the new version on [bintray](https://dl.bintray.com/pmd/pmd-eclipse-plugin/) and
    [github releases](https://github.com/pmd/pmd-eclipse-plugin/releases)


#### Script

    # Pick a release BUILDQUALIFIER (e.g. v20170401-0001) and update versions
    # E.g. version is: "4.0.13" and BUILDQUALIFIER is "v20170401-0001".
    # The complete version of the plugin will be "4.0.13.v20170401-0001
    export BUILDQUALIFIER=$(date -u +v%Y%m%d-%H%M) && echo $BUILDQUALIFIER
    
    # Pick the version of the new release and the next development version
    export VERSION=4.0.18
    export NEXT=4.0.19
    
    echo Update the ReleaseNotes with the release date and version:
    echo 
    echo "## $(date -u +%d-%B-%Y): $VERSION.$BUILDQUALIFIER"
    echo
    echo
    echo "Press enter to continue..."
    read
    ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$VERSION.$BUILDQUALIFIER
    sed -i -e "s/$VERSION.qualifier/$VERSION.$BUILDQUALIFIER/" net.sourceforge.pmd.eclipse.p2updatesite/category.xml
    git commit -a -m "Prepare release pmd-eclipse-plugin $VERSION.$BUILDQUALIFIER"
    git tag $VERSION.$BUILDQUALIFIER
    echo "Create (temporary) release branch"
    git branch pmd-eclipse-plugin-rb-$VERSION
    
    echo
    echo Update the ReleaseNotes and add a next version entry:
    echo "## ????: $NEXT.v????"
    echo
    echo
    echo Press enter...
    read
    
    echo "Updating version in master to next"
    ./mvnw -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$NEXT-SNAPSHOT
    sed -i -e "s/$VERSION.$BUILDQUALIFIER/$NEXT.qualifier/" net.sourceforge.pmd.eclipse.p2updatesite/category.xml
    git commit -a -m "Prepare next pmd-eclipse-plugin development version $NEXT-SNAPSHOT"
    
    echo Checkout the release branch and build the plugin
    git checkout pmd-eclipse-plugin-rb-$VERSION
    
    ./mvnw clean verify
    
    echo
    echo "Please test now!!!"
    echo
    echo Update-Site: file://`pwd`/net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-$VERSION.$BUILDQUALIFIER.zip
    echo
    read
    
    echo
    echo "Publishing now..."
    git checkout master
    git branch -D pmd-eclipse-plugin-rb-$VERSION
    git push origin master
    git push origin tag $VERSION.$BUILDQUALIFIER
    echo
    echo
    
    echo
    echo Update the marketplace entry with the new version:
    echo https://marketplace.eclipse.org/content/pmd-eclipse-plugin
    echo
    
    echo Done.

Also, don't forget to create a [News](https://sourceforge.net/p/pmd/news/) and
verify, that the zipped update site has been uploaded to
[GitHub Releases](https://github.com/pmd/pmd-eclipse-plugin/releases).

You can use the following template:

    PMD for Eclipse $VERSION.$BUILDQUALIFIER released
    
    A new PMD for Eclipse plugin version has been released.
    It is available via the update site: <https://dl.bintray.com/pmd/pmd-eclipse-plugin/updates/>
    
    * Release Notes: <https://github.com/pmd/pmd-eclipse-plugin/blob/$VERSION.$BUILDQUALIFIER/ReleaseNotes.md>



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
