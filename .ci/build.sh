#!/usr/bin/env bash

source $(dirname $0)/inc/github-releases-api.bash || exit 1

# Exit this script immediately if a command/function exits with a non-zero status.
set -e

function build() {
    fetch_ci_scripts

    log_group_start "Install OpenJDK"
        pmd_ci_openjdk_install_adoptopenjdk 8
        pmd_ci_openjdk_setdefault 8
    log_group_end

    log_group_start "Install xvfb"
        #see https://github.com/GabrielBB/xvfb-action
        sudo apt-get install --yes xvfb
    log_group_end

    log_group_start "Determine project name + version"
        pmd_ci_maven_get_project_name
        local name="${RESULT}"
        pmd_ci_maven_get_project_version
        local version="${RESULT}"
    log_group_end

    echo
    echo
    log_info "======================================================================="
    log_info "Building ${name} ${version}"
    log_info "======================================================================="
    pmd_ci_determine_build_env pmd/pmd-eclipse-plugin
    echo
    echo

    if pmd_ci_is_fork_or_pull_request; then
        log_group_start "Build with mvnw"
        xvfb-run --auto-servernum ./mvnw clean verify -B -V -e
        log_group_end
        exit 0
    fi

    # only builds on pmd/build-tools continue here
    log_group_start "Setup environment"
        # PMD_SF_USER, BINTRAY_USER, BINTRAY_APIKEY, GITHUB_OAUTH_TOKEN, GITHUB_BASE_URL
        pmd_ci_secrets_load_private_env
        pmd_ci_secrets_setup_ssh
        pmd_ci_maven_setup_settings
    log_group_end

    if [[ "${version}" == *-SNAPSHOT && "${PMD_CI_BRANCH}" != "" ]]; then
        log_group_start "Snapshot Build: ${version}"
        log_info "This is a snapshot build on branch ${PMD_CI_BRANCH} (version: ${version})"

        # Uploading the update site to Bintray
        xvfb-run --auto-servernum ./mvnw clean verify -B -V -e -Psnapshot-properties -Prelease-composite

        # Cleanup old snapshots
        (
            cd net.sourceforge.pmd.eclipse.p2updatesite
            ./cleanup-bintray-snapshots.sh
        )
        log_group_end

    elif [[ "${version}" != *-SNAPSHOT && "${PMD_CI_TAG}" != "" ]]; then
        log_group_start "Release Build: ${version}"
        log_info "This is a release build for tag ${PMD_CI_TAG} (version: ${version})"

        # create a draft github release
        gh_releases_createDraftRelease "${PMD_CI_TAG}" "$(git rev-list -n 1 ${PMD_CI_TAG})"
        GH_RELEASE="$RESULT"

        # Deploy the update site to bintray
        xvfb-run --auto-servernum ./mvnw clean verify -B -V -e -Prelease-composite

        # Deploy to github releases
        gh_release_uploadAsset "$GH_RELEASE" "net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-${version}.zip"

        # extract the release notes
        RELEASE_NAME="PMD For Eclipse ${version} ($(date -u +%d-%B-%Y))"
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

        log_group_end

    else
        log_error "Invalid combination: version=${version} branch=${PMD_CI_BRANCH} tag=${PMD_CI_TAG}"
        exit 1
    fi
}

function fetch_ci_scripts() {
    mkdir -p .ci/inc

    for f in \
                logger.bash \
                utils.bash \
                openjdk.bash \
                secrets.bash \
                maven.bash \
            ; do
        if [ ! -e .ci/inc/$f ]; then
            curl -sSL https://raw.githubusercontent.com/pmd/build-tools/master/.ci/inc/$f > .ci/inc/$f
        fi
        source .ci/inc/$f || exit 1
    done
}

build
