#!/bin/bash
set -e

source .travis/logger.sh
source .travis/github-releases-api.sh

VERSION=$(./mvnw -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.5.0:exec | tail -1)

echo "Performing build steps"
echo "TRAVIS_REPO_SLUG: $TRAVIS_REPO_SLUG"
echo "TRAVIS_PULL_REQUEST_SLUG: $TRAVIS_PULL_REQUEST_SLUG"
echo "TRAVIS_PULL_REQUEST_BRANCH: $TRAVIS_PULL_REQUEST_BRANCH"
echo "TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"
echo "TRAVIS_SECURE_ENV_VARS: $TRAVIS_SECURE_ENV_VARS"
echo "TRAVIS_BRANCH: $TRAVIS_BRANCH"
echo "TRAVIS_TAG: $TRAVIS_TAG"
echo "TRAVIS_ALLOW_FAILURE: $TRAVIS_ALLOW_FAILURE"

if [ "${TRAVIS_REPO_SLUG}" != "pmd/pmd-eclipse-plugin" ] || [ "${TRAVIS_PULL_REQUEST}" != "false" ]; then

    log_info "This is a pull-request build"
    ./mvnw verify

elif [ "${TRAVIS_REPO_SLUG}" = "pmd/pmd-eclipse-plugin" ] && [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_SECURE_ENV_VARS}" = "true" ]; then

    if [[ "${VERSION}" != *-SNAPSHOT && "${TRAVIS_TAG}" != "" ]]; then
        log_info "This is a release build for tag ${TRAVIS_TAG} (version: ${VERSION})"

        # create a draft github release
        gh_releases_createDraftRelease "${TRAVIS_TAG}" "$(git show-ref --hash ${TRAVIS_TAG})"
        GH_RELEASE="$RESULT"

        # Deploy the update site to bintray
        ./mvnw clean verify -Prelease-composite

        # Deploy to github releases
        gh_release_uploadAsset "$GH_RELEASE" "net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-${VERSION}.zip"

        # extract the release notes
        RELEASE_NAME="PMD For Eclipse ${VERSION} ($(date -u +%d-%B-%Y))"
        BEGIN_LINE=$(grep -n "^## " ReleaseNotes.md|head -1|cut -d ":" -f 1)
        END_LINE=$(grep -n "^## " ReleaseNotes.md|head -2|tail -1|cut -d ":" -f 1)
        END_LINE=$((END_LINE - 1))

        RELEASE_BODY="A new PMD for Eclipse plugin version has been released.
It is available via the update site: https://dl.bintray.com/pmd/pmd-eclipse-plugin/updates/

$(cat ReleaseNotes.md|head -$END_LINE|tail -$((END_LINE - BEGIN_LINE)))
"

        gh_release_updateRelease "$GH_RELEASE" "$RELEASE_NAME" "$RELEASE_BODY"

        # Publish release
        gh_release_publishRelease "$GH_RELEASE"

    elif [[ "${VERSION}" == *-SNAPSHOT ]]; then
        log_info "This is a snapshot build (version: ${VERSION})"

        # Uploading the update site to Bintray
        ./mvnw verify -DskipTests -Psnapshot-properties -Prelease-composite
        # Cleanup old snapshots
        (
            cd net.sourceforge.pmd.eclipse.p2updatesite
            ./cleanup-bintray-snapshots.sh
        )

    else
        # other build. Can happen during release: the commit with a non snapshot version is built, but not from the tag.
        log_info "This is some other build, probably during release: commit with a non-snapshot version on branch master..."
        # we stop here - no need to execute further steps
        exit 0
    fi

else
    log_info "This is neither a pull request nor a push. Not executing any build."
    exit 1
fi

