#!/usr/bin/env bash

# Exit this script immediately if a command/function exits with a non-zero status.
set -e

SCRIPT_INCLUDES="log.bash utils.bash setup-secrets.bash openjdk.bash maven.bash sourceforge-api.bash github-releases-api.bash"
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

    # stop early for invalid maven version and branch/tag combination
    pmd_ci_maven_verify_version || exit 0

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

        # Build and upload the update site to Bintray
        xvfb-run --auto-servernum ./mvnw clean verify --show-version --errors --batch-mode \
            --no-transfer-progress \
            --activate-profiles snapshot-properties,release-composite

        local qualifiedVersion
        qualifiedVersion="$(basename net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-*.zip)"
        qualifiedVersion="${qualifiedVersion%.zip}"
        qualifiedVersion="${qualifiedVersion#net.sourceforge.pmd.eclipse.p2updatesite-}"
        mv "net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-${qualifiedVersion}.zip" "net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-SNAPSHOT.zip"
        pmd_ci_sourceforge_uploadFile "pmd-eclipse/zipped" "net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-SNAPSHOT.zip"

        # Cleanup old snapshots
        (
            cd net.sourceforge.pmd.eclipse.p2updatesite
            ./cleanup-bintray-snapshots.sh
        )
    pmd_ci_log_group_end

    pmd_ci_log_group_start "Add snapshot to update site"
        pmd_ci_log_info "Updating pmd-eclipse-plugin-p2-site..."
        prepare_local_p2_site
        (
            cd current-p2-site

            rm -rf snapshot
            unzip -q -d snapshot "../net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-SNAPSHOT.zip"
            echo "This is a Eclipse Update Site for the [PMD Eclipse Plugin](https://github.com/pmd/pmd-eclipse-plugin/) ${PMD_CI_MAVEN_PROJECT_VERSION}.

Use <https://pmd.github.io/pmd-eclipse-plugin-p2-site/snapshot/> to install the plugin with the Eclipse Update Manager.

<dl>
  <dt>Feature ID</dt>
  <dd>net.sourceforge.pmd.eclipse</dd>
  <dt>Version</dt>
  <dd>${qualifiedVersion}</dd>
</dl>

" > snapshot/index.md
            git add snapshot

            # create a new single commit
            git checkout --orphan=gh-pages-2
            git commit -a -m "Update pmd/pmd-eclipse-plugin-p2-site"
            git push --force origin gh-pages-2:gh-pages
            pmd_ci_log_success "Successfully updated https://pmd.github.io/pmd-eclipse-plugin-p2-site/"
        )

    pmd_ci_log_group_end
}

function release_build() {
    pmd_ci_log_group_start "Release Build: ${PMD_CI_MAVEN_PROJECT_VERSION}"
        pmd_ci_log_info "This is a release build for tag ${PMD_CI_TAG} (version: ${PMD_CI_MAVEN_PROJECT_VERSION})"

        # Build and deploy the update site to bintray
        xvfb-run --auto-servernum ./mvnw clean verify --show-version --errors --batch-mode \
            --no-transfer-progress \
            --activate-profiles release-composite
    pmd_ci_log_group_end

    pmd_ci_log_group_start "Update Github Releases"
        # create a draft github release
        pmd_ci_gh_releases_createDraftRelease "${PMD_CI_TAG}" "$(git rev-list -n 1 "${PMD_CI_TAG}")"
        GH_RELEASE="$RESULT"

        # Deploy to github releases
        pmd_ci_gh_releases_uploadAsset "$GH_RELEASE" "net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-${PMD_CI_MAVEN_PROJECT_VERSION}.zip"

        # extract the release notes
        RELEASE_NAME="PMD For Eclipse ${PMD_CI_MAVEN_PROJECT_VERSION}"
        BEGIN_LINE=$(grep -n "^## " ReleaseNotes.md|head -1|cut -d ":" -f 1)
        END_LINE=$(grep -n "^## " ReleaseNotes.md|head -2|tail -1|cut -d ":" -f 1)
        END_LINE=$((END_LINE - 1))

        RELEASE_BODY="A new PMD for Eclipse plugin version has been released.
It is available via the update site: https://pmd.github.io/pmd-eclipse-plugin-p2-site/

$(head -$END_LINE ReleaseNotes.md | tail -$((END_LINE - BEGIN_LINE)))
"

        pmd_ci_gh_releases_updateRelease "$GH_RELEASE" "$RELEASE_NAME" "$RELEASE_BODY"

        # Upload it to sourceforge
        pmd_ci_sourceforge_uploadFile "pmd-eclipse/zipped" "net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-${PMD_CI_MAVEN_PROJECT_VERSION}.zip"

        # Create sourceforge blog entry
        pmd_ci_sourceforge_createDraftBlogPost "$RELEASE_NAME released" "$RELEASE_BODY" "pmd-eclipse-plugin,release"
        local sf_blog_url="${RESULT}"
    pmd_ci_log_group_end

    pmd_ci_log_group_start "Add new release to update site"
        pmd_ci_log_info "Updating pmd-eclipse-plugin-p2-site..."
        prepare_local_p2_site
        (
            cd current-p2-site

            unzip -q -d "${PMD_CI_MAVEN_PROJECT_VERSION}" "net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-${PMD_CI_MAVEN_PROJECT_VERSION}.zip"
            git add "${PMD_CI_MAVEN_PROJECT_VERSION}"
            regenerate_metadata

            # create a new single commit
            git checkout --orphan=gh-pages-2
            git commit -a -m "Update pmd/pmd-eclipse-plugin-p2-site"
            git push --force origin gh-pages-2:gh-pages
            pmd_ci_log_success "Successfully updated https://pmd.github.io/pmd-eclipse-plugin-p2-site/"
        )
    pmd_ci_log_group_end

    # Publish release - this sends out notifications on github
    pmd_ci_gh_releases_publishRelease "$GH_RELEASE"
    # Publish sourceforge blog entry
    pmd_ci_sourceforge_publishBlogPost "${sf_blog_url}"
}


function prepare_local_p2_site() {
    pmd_ci_log_info "Preparing local copy of p2-site..."
    rm -rf current-p2-site
    mkdir current-p2-site
    (
        cd current-p2-site
        git init -q --initial-branch=gh-pages
        git config user.name "PMD CI (pmd-bot)"
        git config user.email "andreas.dangel+pmd-bot@adangel.org"
        git remote add origin git@github.com:pmd/pmd-eclipse-plugin-p2-site.git
        git pull --rebase origin gh-pages
    )
}

function regenerate_metadata() {
    pmd_ci_log_info "Regenerating metadata for p2-site..."
    local releases=($(find . -maxdepth 1 -type d -regex "\./[0-9]+\.[0-9]+\.[0-9]+\..*" -printf '%f\n'| tr '.' '\0' | sort -t '\0' -k1,1nr -k2,2nr -k3,3nr -k4dr |awk -F '\0' '{printf "%s.%s.%s.%s\n", $1, $2, $3, $4}'))
    # remove old releases
    for i in "${releases[@]:5}"; do
      pmd_ci_log_info "Removing old release $i..."
      rm -rf "$i"
    done
    releases=("${releases[@]:0:5}")

    # regenerate metadata
    local now
    now=$(date +%s000)
    local children=""
    local children_index=""
    for i in "${releases[@]}"; do
      children="${children}    <child location=\"$i\"/>\n"
      children_index="${children_index}  * [$i]($i/)\n"
      echo "This is a Eclipse Update Site for the [PMD Eclipse Plugin](https://github.com/pmd/pmd-eclipse-plugin/) ${i}.

Use <https://pmd.github.io/pmd-eclipse-plugin-p2-site/${i}/> to install the plugin with the Eclipse Update Manager.

<dl>
  <dt>Feature ID</dt>
  <dd>net.sourceforge.pmd.eclipse</dd>
  <dt>Version</dt>
  <dd>${i}</dd>
</dl>

" > "$i"/index.md

      git add "$i"/index.md
    done

    local site_name="PMD for Eclipse - Update Site"
    local artifactsTemplate="<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<?compositeArtifactRepository version=\"1.0.0\"?>
<repository name=\"${site_name}\" type=\"org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository\" version=\"1.0.0\">
  <properties size=\"2\">
    <property name=\"p2.timestamp\" value=\"${now}\"/>
    <property name=\"p2.atomic.composite.loading\" value=\"true\"/>
  </properties>
  <children size=\"${#releases[@]}\">
${children}  </children>
</repository>"
    echo -e "${artifactsTemplate}" > compositeArtifacts.xml

    local contentTemplate="<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<?compositeMetadataRepository version=\"1.0.0\"?>
<repository name=\"${site_name}\" type=\"org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository\" version=\"1.0.0\">
  <properties size=\"2\">
    <property name=\"p2.timestamp\" value=\"${now}\"/>
    <property name=\"p2.atomic.composite.loading\" value=\"true\"/>
  </properties>
  <children size=\"${#releases[@]}\">
${children}  </children>
</repository>"
    echo -e "${contentTemplate}" > compositeContent.xml

    # p2.index
    local p2_index="version = 1
metadata.repository.factory.order = compositeContent.xml,\!
artifact.repository.factory.order = compositeArtifacts.xml,\!"
    echo -e "${p2_index}" > p2.index

    # regenerate index.md
    echo -e "This is a composite Eclipse Update Site for the [PMD Eclipse Plugin](https://github.com/pmd/pmd-eclipse-plugin/).

Use <https://pmd.github.io/pmd-eclipse-plugin-p2-site/> to install the plugin with the Eclipse Update Manager.

----

Versions available at <https://pmd.github.io/pmd-eclipse-plugin-p2-site/>:

${children_index}

For older versions, see <https://sourceforge.net/projects/pmd/files/pmd-eclipse/zipped/>

" > index.md
}

build
