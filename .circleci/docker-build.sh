#!/bin/sh
DIR=$(dirname "$(readlink -f "$0")")
export TAG=$CIRCLE_SHA1
. $DIR/transform_vars.sh
mkdir -p /tmp/workspace/
# Build docker image to tar
./gradlew jibBuildTar
# Put it where it will be persisted
mv build/jib-image.tar /tmp/workspace
