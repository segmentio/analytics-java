#!/bin/sh

set -e

java -jar analytics-cli/target/analytics-cli-*-jar-with-dependencies.jar "$@"
