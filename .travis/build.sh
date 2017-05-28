#!/bin/bash
set -e

echo "Performing build steps"
echo "TRAVIS_REPO_SLUG: $TRAVIS_REPO_SLUG"
echo "TRAVIS_PULL_REQUEST_SLUG: $TRAVIS_PULL_REQUEST_SLUG"
echo "TRAVIS_PULL_REQUEST_BRANCH: $TRAVIS_PULL_REQUEST_BRANCH"
echo "TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"
echo "TRAVIS_SECURE_ENV_VARS: $TRAVIS_SECURE_ENV_VARS"
echo "TRAVIS_BRANCH: $TRAVIS_BRANCH"
echo "TRAVIS_TAG: $TRAVIS_TAG"
echo "TRAVIS_ALLOW_FAILURE: $TRAVIS_ALLOW_FAILURE"


./mvnw verify


if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_SECURE_ENV_VARS}" = "true" ]; then
    # Uploading the update site to sourceforge
    rsync -avh --delete net.sourceforge.pmd.eclipse.p2updatesite/target/repository/ ${PMD_SF_USER}@web.sourceforge.net:/home/frs/project/pmd/pmd-eclipse/update-site-latest/
    rsync -avh net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-*.zip ${PMD_SF_USER}@web.sourceforge.net:/home/frs/project/pmd/pmd-eclipse/update-site-latest/net.sourceforge.pmd.eclipse.p2updatesite-LATEST.zip
fi
