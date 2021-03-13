#!/usr/bin/env bash

# Exit this script immediately if a command/function exits with a non-zero status.
set -e

SCRIPT_INCLUDES="log.bash utils.bash setup-secrets.bash openjdk.bash maven.bash sourceforge-api.bash"
# shellcheck source=inc/fetch_ci_scripts.bash
source "$(dirname "$0")/inc/fetch_ci_scripts.bash" && fetch_ci_scripts

function build() {
    pmd_ci_log_group_start "Install OpenJDK"
        pmd_ci_openjdk_install_adoptopenjdk 8
        pmd_ci_openjdk_setdefault 8
    pmd_ci_log_group_end

    pmd_ci_log_group_start "Install xvfb"
        #see https://github.com/GabrielBB/xvfb-action
        sudo apt-get install --yes xvfb
        sudo apt-get install --yes libgtk2.0-0
    pmd_ci_log_group_end

    echo
    pmd_ci_maven_display_info_banner
    pmd_ci_utils_determine_build_env pmd/pmd-eclipse-plugin
    echo

    if pmd_ci_utils_is_fork_or_pull_request; then
        pmd_ci_log_group_start "Build with mvnw"
            xvfb-run --auto-servernum ./mvnw clean verify --show-version --errors --batch-mode --no-transfer-progress
        pmd_ci_log_group_end
        exit 0
    fi

    # only builds on pmd/pmd-eclipse-plugin continue here
    pmd_ci_log_group_start "Setup environment"
        pmd_ci_setup_secrets_private_env
        pmd_ci_setup_secrets_ssh
        pmd_ci_maven_setup_settings
    pmd_ci_log_group_end

    if pmd_ci_maven_isSnapshotBuild; then
        snapshot_build
    elif pmd_ci_maven_isReleaseBuild; then
        release_build
    else
        pmd_ci_log_error "Invalid combination: version=${PMD_CI_MAVEN_PROJECT_VERSION} branch=${PMD_CI_BRANCH} tag=${PMD_CI_TAG}"
        exit 1
    fi
}

function snapshot_build() {
    pmd_ci_log_group_start "Snapshot Build: ${PMD_CI_MAVEN_PROJECT_VERSION}"
        pmd_ci_log_info "This is a snapshot build on branch ${PMD_CI_BRANCH} (version: ${PMD_CI_MAVEN_PROJECT_VERSION})"

        # Uploading the update site to Bintray
        xvfb-run --auto-servernum ./mvnw clean verify --show-version --errors --batch-mode \
            --no-transfer-progress \
            --activate-profiles snapshot-properties,release-composite

        # Cleanup old snapshots
        (
            cd net.sourceforge.pmd.eclipse.p2updatesite
            ./cleanup-bintray-snapshots.sh
        )
    pmd_ci_log_group_end
}

function release_build() {
    pmd_ci_log_group_start "Release Build: ${PMD_CI_MAVEN_PROJECT_VERSION}"
        pmd_ci_log_info "This is a release build for tag ${PMD_CI_TAG} (version: ${PMD_CI_MAVEN_PROJECT_VERSION})"

        # create a draft github release
        pmd_ci_gh_releases_createDraftRelease "${PMD_CI_TAG}" "$(git rev-list -n 1 "${PMD_CI_TAG}")"
        GH_RELEASE="$RESULT"

        # Deploy the update site to bintray
        xvfb-run --auto-servernum ./mvnw clean verify --show-version --errors --batch-mode \
            --no-transfer-progress \
            --activate-profiles release-composite

        # Deploy to github releases
        pmd_ci_gh_releases_uploadAsset "$GH_RELEASE" "net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-${PMD_CI_MAVEN_PROJECT_VERSION}.zip"

        # extract the release notes
        RELEASE_NAME="PMD For Eclipse ${PMD_CI_MAVEN_PROJECT_VERSION} ($(date -u +%d-%B-%Y))"
        BEGIN_LINE=$(grep -n "^## " ReleaseNotes.md|head -1|cut -d ":" -f 1)
        END_LINE=$(grep -n "^## " ReleaseNotes.md|head -2|tail -1|cut -d ":" -f 1)
        END_LINE=$((END_LINE - 1))

        RELEASE_BODY="A new PMD for Eclipse plugin version has been released.
It is available via the update site: https://dl.bintray.com/pmd/pmd-eclipse-plugin/updates/

$(head -$END_LINE ReleaseNotes.md | tail -$((END_LINE - BEGIN_LINE)))
"

        pmd_ci_gh_releases_updateRelease "$GH_RELEASE" "$RELEASE_NAME" "$RELEASE_BODY"

        # Upload it to sourceforge
        pmd_ci_sourceforge_uploadFile "pmd-eclipse/zipped" "net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-${PMD_CI_MAVEN_PROJECT_VERSION}.zip"

        # Publish release
        pmd_ci_gh_releases_publishRelease "$GH_RELEASE"

    pmd_ci_log_group_end
}

build
