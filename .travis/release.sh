#!/bin/bash
set -e

RELEASE_VERSION=$(./mvnw -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.5.0:exec | tail -1)

echo "Performing release steps"
echo "RELEASE_VERSION: $RELEASE_VERSION"
echo "TRAVIS_REPO_SLUG: $TRAVIS_REPO_SLUG"
echo "TRAVIS_PULL_REQUEST_SLUG: $TRAVIS_PULL_REQUEST_SLUG"
echo "TRAVIS_PULL_REQUEST_BRANCH: $TRAVIS_PULL_REQUEST_BRANCH"
echo "TRAVIS_PULL_REQUEST: $TRAVIS_PULL_REQUEST"
echo "TRAVIS_SECURE_ENV_VARS: $TRAVIS_SECURE_ENV_VARS"
echo "TRAVIS_BRANCH: $TRAVIS_BRANCH"
echo "TRAVIS_TAG: $TRAVIS_TAG"
echo "TRAVIS_ALLOW_FAILURE: $TRAVIS_ALLOW_FAILURE"


# Deploy the update site to bintray
./mvnw clean verify -Prelease-composite

# Assumes, the release has already been created by travis github releases provider
GITHUB_URL="https://api.github.com/repos/pmd/pmd-eclipse-plugin/releases"
RELEASE_ID=$(curl -s -H "Authorization: token ${GITHUB_OAUTH_TOKEN}" ${GITHUB_URL}/tags/${TRAVIS_TAG}|jq ".id")
RELEASE_NAME="PMD For Eclipse ${RELEASE_VERSION} ($(date -u +%d-%B-%Y))"
RELEASE_BODY="A new PMD for Eclipse plugin version has been released.
It is available via the update site: https://dl.bintray.com/pmd/pmd-eclipse-plugin/updates/

Release notes: https://github.com/pmd/pmd-eclipse-plugin/blob/${TRAVIS_TAG}/ReleaseNotes.md
"

RELEASE_BODY="${RELEASE_BODY//'\'/\\\\}"
RELEASE_BODY="${RELEASE_BODY//$'\r'/}"
RELEASE_BODY="${RELEASE_BODY//$'\n'/\\r\\n}"
RELEASE_BODY="${RELEASE_BODY//'"'/\\\"}"
cat > release-edit-request.json <<EOF
{
  "name": "$RELEASE_NAME",
  "body": "$RELEASE_BODY"
}
EOF
echo "Updating release at ${GITHUB_URL}/${RELEASE_ID}..."


RESPONSE=$(curl -i -s -H "Authorization: token ${GITHUB_OAUTH_TOKEN}" -H "Content-Type: application/json" --data "@release-edit-request.json" -X PATCH ${GITHUB_URL}/${RELEASE_ID})
if [[ "$RESPONSE" != *"HTTP/1.1 200"* ]]; then
    echo "Request:"
    cat release-edit-request.json
    echo
    echo "Response:"
    echo "$RESPONSE"
else
    echo "Update OK"
fi
