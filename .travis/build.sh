#!/bin/bash
set -e

./mvnw verify

# Uploading the update site to sourceforge
rsync -avh --delete net.sourceforge.pmd.eclipse.p2updatesite/target/repository/ ${PMD_SF_USER}@web.sourceforge.net:/home/frs/project/pmd/pmd-eclipse/update-site-latest/
rsync -avh net.sourceforge.pmd.eclipse.p2updatesite/target/net.sourceforge.pmd.eclipse.p2updatesite-*.zip ${PMD_SF_USER}@web.sourceforge.net:/home/frs/project/pmd/pmd-eclipse/update-site-latest/net.sourceforge.pmd.eclipse.p2updatesite-LATEST.zip
