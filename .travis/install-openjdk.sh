#!/bin/bash
set -e

source .travis/logger.sh

DOWNLOAD_URL="https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u232-b09/OpenJDK8U-jdk_x64_linux_hotspot_8u232b09.tar.gz"
OPENJDK_ARCHIVE=$(basename $DOWNLOAD_URL)

LOCAL_DIR=${HOME}/.cache/openjdk
TARGET_DIR=${HOME}/openjdk${OPENJDK_MAJOR}

mkdir -p ${LOCAL_DIR}
mkdir -p ${TARGET_DIR}
if [ ! -e ${LOCAL_DIR}/${OPENJDK_ARCHIVE} ]; then
    log_info "Downloading from ${DOWNLOAD_URL} to ${LOCAL_DIR}"
    wget --directory-prefix ${LOCAL_DIR} --timestamping --continue ${DOWNLOAD_URL}
else
    log_info "Skipped download, file ${LOCAL_DIR}/${OPENJDK_ARCHIVE} already exists"
fi

log_info "Extracting to ${TARGET_DIR}"
tar --extract --file ${LOCAL_DIR}/${OPENJDK_ARCHIVE} -C ${TARGET_DIR} --strip-components=1

cat > ${HOME}/java.env <<EOF
export JAVA_HOME="${TARGET_DIR}"
export PATH="${TARGET_DIR}/bin:${PATH}"
java -version
EOF

log_info "OpenJDK can be used via ${HOME}/java.env"
cat ${HOME}/java.env
source ${HOME}/java.env
