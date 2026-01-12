#!/bin/bash
#
# Deploy a jar, source jar, and javadoc jar to Sonatype's snapshot repo.
#
# Adapted from https://coderwall.com/p/9b_lfq and
# http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/

REPO="analytics-java"
USERNAME="segmentio"
JDK="oraclejdk8"
BRANCH="master"

set -e

if [ "$CIRCLE_PROJECT_REPONAME" != "$REPO" ]; then
  echo "Skipping snapshot deployment: wrong repository. Expected '$REPO' but was '$CIRCLE_PROJECT_REPONAME'."
elif [ "$CIRCLE_PROJECT_USERNAME" != "$USERNAME" ]; then
  echo "Skipping snapshot deployment: wrong owner. Expected '$USERNAME' but was '$CIRCLE_PROJECT_USERNAME'."
elif [ "$CIRCLE_JDK_VERSION" != "$JDK" ]; then
  # $CIRCLE_JDK_VERSION must be manually set in circle.yml
  echo "Skipping snapshot deployment: wrong JDK. Expected '$JDK' but was '$CIRCLE_JDK_VERSION'."
elif [ "$CIRCLE_BRANCH" != "$BRANCH" ]; then
  echo "Skipping snapshot deployment: wrong branch. Expected '$BRANCH' but was '$CIRCLE_BRANCH'."
else
  echo "Deploying snapshot..."
  mvn clean source:jar javadoc:jar deploy --settings=".buildscript/settings.xml" -Dmaven.test.skip=true -Dgpg.skip=true
  echo "Snapshot deployed!"
fi
