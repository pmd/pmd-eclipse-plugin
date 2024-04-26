#!/usr/bin/env bash
set -e

# Make sure, everything is English...
export LANG=en_US.UTF-8

# verify the current directory
PMD_GITHUB_IO_DIR="../pmd.github.io"
if [ ! -f pom.xml ] || [ ! -d ${PMD_GITHUB_IO_DIR} ]; then
    echo "You seem to be in the wrong working directory or you don't have pmd.github.io checked out..."
    echo
    echo "Expected:"
    echo "*   You are currently in the pmd-eclipse-plugin repository"
    echo "*   ${PMD_GITHUB_IO_DIR} is the pmd.github.io repository"
    echo
    exit 1
fi

RELEASE_VERSION=
CURRENT_BRANCH=

echo "-------------------------------------------"
echo "Releasing PMD Eclipse Plugin"
echo "-------------------------------------------"

CURRENT_VERSION=$(./mvnw org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.version -q -DforceStdout)
RELEASE_VERSION=${CURRENT_VERSION%-SNAPSHOT}
MAJOR=$(echo "$RELEASE_VERSION" | cut -d . -f 1)
MINOR=$(echo "$RELEASE_VERSION" | cut -d . -f 2)
PATCH=$(echo "$RELEASE_VERSION" | cut -d . -f 3)
if [ "$PATCH" == "0" ]; then
    NEXT_MINOR=$(("${MINOR}" + 1))
    NEXT_PATCH="0"
else
    # this is a bugfixing release
    NEXT_MINOR="${MINOR}"
    NEXT_PATCH=$(("${PATCH}" + 1))
fi

# allow to override the next version, e.g. via "DEVELOPMENT_VERSION=7.0.0 ./do-release.sh"
if [ "$DEVELOPMENT_VERSION" = "" ]; then
    DEVELOPMENT_VERSION="$MAJOR.$NEXT_MINOR.$NEXT_PATCH"
fi

# allow to override the build qualifier, e.g. via "BUILDQUALIFIER="$(date -u +v%Y%m%d-%H%M)-rc1" ./do-release.sh"
if [ "$BUILDQUALIFIER" = "" ]; then
    # Pick a release BUILDQUALIFIER (e.g. v20170401-0001-r) and update versions
    # E.g. version is: "4.0.13" and BUILDQUALIFIER is "v20170401-0001-r".
    # The complete version of the plugin will be "4.0.13.v20170401-0001-r
    BUILDQUALIFIER="$(date -u +v%Y%m%d-%H%M)-r"
fi

# http://stackoverflow.com/questions/1593051/how-to-programmatically-determine-the-current-checked-out-git-branch
CURRENT_BRANCH=$(git symbolic-ref -q HEAD)
CURRENT_BRANCH=${CURRENT_BRANCH##refs/heads/}
CURRENT_BRANCH=${CURRENT_BRANCH:-HEAD}

export DEVELOPMENT_VERSION
export BUILDQUALIFIER

echo "RELEASE_VERSION: ${RELEASE_VERSION}.${BUILDQUALIFIER} (this release)"
echo "DEVELOPMENT_VERSION: ${DEVELOPMENT_VERSION}-SNAPSHOT (the next version after the release)"
echo "CURRENT_BRANCH: ${CURRENT_BRANCH}"

echo
echo "Is this correct?"
echo
echo "Press enter to continue... (or CTRL+C to cancel)"
read -r

echo Update the ReleaseNotes with the release date and version:
echo 
echo "## $(date -u +%d-%B-%Y): ${RELEASE_VERSION}.${BUILDQUALIFIER}"
echo
echo "And also remove any empty/unnecessary sections"
echo
echo "Press enter to continue..."
read -r
./mvnw org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion="${RELEASE_VERSION}.${BUILDQUALIFIER}"
git commit -a -m "Prepare release pmd-eclipse-plugin ${RELEASE_VERSION}.${BUILDQUALIFIER}"
git tag -m "Release version ${RELEASE_VERSION}.${BUILDQUALIFIER}" "${RELEASE_VERSION}.${BUILDQUALIFIER}"
echo "Create (temporary) release branch"
git branch "pmd-eclipse-plugin-rb-${RELEASE_VERSION}"

echo
echo Updating the ReleaseNotes and add a next version entry...
BEGIN_LINE=$(grep -n "^## " ReleaseNotes.md|head -1|cut -d ":" -f 1)
BEGIN_LINE=$((BEGIN_LINE - 1))
RELEASE_NOTES_HEADER=$(head -$BEGIN_LINE ReleaseNotes.md)
RELEASE_NOTES_BODY=$(tail --lines=+$BEGIN_LINE ReleaseNotes.md)

cat <<EOF > ReleaseNotes.md
${RELEASE_NOTES_HEADER}

## ????: ${DEVELOPMENT_VERSION}.v????

This is a minor release.

### New and noteworthy

### Fixed Issues

### API Changes

### External Contributions
${RELEASE_NOTES_BODY}

EOF

echo
echo "Updating version in master to next"
./mvnw org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion="${DEVELOPMENT_VERSION}-SNAPSHOT"
git commit -a -m "Prepare next pmd-eclipse-plugin development version ${DEVELOPMENT_VERSION}-SNAPSHOT"
echo "Pushing master"
git push origin master

echo
echo Checkout the release branch and build the plugin
git checkout "pmd-eclipse-plugin-rb-${RELEASE_VERSION}"

./mvnw clean verify

# extract the release notes
BEGIN_LINE=$(grep -n "^## " ReleaseNotes.md|head -1|cut -d ":" -f 1)
END_LINE=$(grep -n "^## " ReleaseNotes.md|head -2|tail -1|cut -d ":" -f 1)
END_LINE=$((END_LINE - 1))

RELEASE_BODY="A new PMD for Eclipse plugin version has been released.
It is available via the update site: https://pmd.github.io/pmd-eclipse-plugin-p2-site/

$(head -$END_LINE ReleaseNotes.md|tail -$((END_LINE - BEGIN_LINE)))
"


echo
echo "Please test now!!!"
echo
echo "Update-site: jar:file:$(pwd)/net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-${RELEASE_VERSION}.${BUILDQUALIFIER}.zip!/"
echo
read -r

echo
echo "Publishing now..."
git restore net.sourceforge.pmd.eclipse.p2updatesite/category.xml
git checkout master
git branch -D "pmd-eclipse-plugin-rb-${RELEASE_VERSION}"
git push origin tag "${RELEASE_VERSION}.${BUILDQUALIFIER}"
echo
echo

echo
echo Wait for github build to finish: https://github.com/pmd/pmd-eclipse-plugin/actions
echo
echo Verify, zipped update site has been uploaded to
echo "  * https://github.com/pmd/pmd-eclipse-plugin/releases"
echo "  * https://pmd.github.io/pmd-eclipse-plugin-p2-site/"
echo "  * https://sourceforge.net/projects/pmd/files/pmd-eclipse/zipped/"
echo "Verify, news entry has been created: https://sourceforge.net/p/pmd/news/"
echo
echo Update the marketplace entry with the new version:
echo https://marketplace.eclipse.org/content/pmd-eclipse-plugin/edit
echo
echo "Check the milestone on github:"
echo "<https://github.com/pmd/pmd-eclipse-plugin/milestones>"
echo " --> move any open issues to the next milestone, close the current milestone"
echo

echo

echo
tweet="PMD for Eclipse ${RELEASE_VERSION}.${BUILDQUALIFIER} released #PMD
Update site: https://pmd.github.io/pmd-eclipse-plugin-p2-site/
Release notes: https://github.com/pmd/pmd-eclipse-plugin/blob/${RELEASE_VERSION}.${BUILDQUALIFIER}/ReleaseNotes.md"
tweet="${tweet// /%20}"
tweet="${tweet//:/%3A}"
tweet="${tweet//#/%23}"
tweet="${tweet//\//%2F}"
tweet="${tweet//$'\r'/}"
tweet="${tweet//$'\n'/%0A}"
echo "Tweet about this release on https://twitter.com/pmd_analyzer:"
echo "        <https://twitter.com/intent/tweet?text=$tweet>"
echo

POST_FILE="_posts/$(date +%Y-%m-%d)-PMD-for-Eclipse-${RELEASE_VERSION}.${BUILDQUALIFIER}-released.md"
cat > "${PMD_GITHUB_IO_DIR}/${POST_FILE}" <<EOF
---
layout: post
title: PMD For Eclipse ${RELEASE_VERSION}.${BUILDQUALIFIER} Released
---

${RELEASE_BODY}
EOF

pushd "${PMD_GITHUB_IO_DIR}"; git add "${POST_FILE}"; popd
echo
echo "Created ${PMD_GITHUB_IO_DIR}/${POST_FILE}"
echo
echo "Please verify and commit and push..."
echo "cd ${PMD_GITHUB_IO_DIR}"
echo "git commit -m \"PMD For Eclipse ${RELEASE_VERSION}.${BUILDQUALIFIER} Released\""
echo "git push origin master"
echo
echo
echo Done.
