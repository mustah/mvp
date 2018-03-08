#!/bin/bash
REPO_URL=$1
BRANCH=$2
TMPDIR=$(mktemp -d)
REPO_PATH="${TMPDIR}/$(echo ${REPOSITORY_URL} | sed 's/[^a-zA-Z0-9]/-/g')"

git clone -q --bare ${REPO_URL} --no-checkout --single-branch --branch $BRANCH ${REPO_PATH}
git --git-dir=${REPO_PATH} describe --tags --abbrev=7 --match *.*.* ${BRANCH}

rm -rf ${TMPDIR}


