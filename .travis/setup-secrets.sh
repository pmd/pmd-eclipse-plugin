#!/bin/bash
set -e

if [ "$TRAVIS_PULL_REQUEST" != "false" ] || [ "${TRAVIS_SECURE_ENV_VARS}" != "true" ]; then
    echo "Not setting up secrets (TRAVIS_PULL_REQUEST=${TRAVIS_PULL_REQUEST} TRAVIS_SECURE_ENV_VARS=${TRAVIS_SECURE_ENV_VARS})."
    exit 0
fi


mkdir -p $HOME/.ssh
openssl aes-256-cbc -K $encrypted_4ffa600c8269_key -iv $encrypted_4ffa600c8269_iv -in .travis/id_rsa.enc -out $HOME/.ssh/id_rsa -d
chmod 600 $HOME/.ssh/id_rsa
