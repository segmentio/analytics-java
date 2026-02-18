#!/bin/bash
#
# Run E2E tests for analytics-java
#
# Prerequisites: Java 11+, Maven, Node.js 18+
#
# Usage:
#   ./run-e2e.sh [extra args passed to run-tests.sh]
#
# Override sdk-e2e-tests location:
#   E2E_TESTS_DIR=../my-e2e-tests ./run-e2e.sh
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SDK_ROOT="$SCRIPT_DIR/.."
E2E_DIR="${E2E_TESTS_DIR:-$SDK_ROOT/../sdk-e2e-tests}"

echo "=== Building analytics-java e2e-cli ==="

# Build SDK and e2e-cli
cd "$SDK_ROOT"
mvn package -pl e2e-cli -am -DskipTests

# Find the built jar
CLI_JAR=$(find "$SDK_ROOT/e2e-cli/target" -name "e2e-cli-*-jar-with-dependencies.jar" | head -1)
if [[ -z "$CLI_JAR" ]]; then
    echo "Error: Could not find e2e-cli jar"
    exit 1
fi
echo "Found jar: $CLI_JAR"

echo ""

# Run tests
cd "$E2E_DIR"
./scripts/run-tests.sh \
    --sdk-dir "$SCRIPT_DIR" \
    --cli "java -jar $CLI_JAR" \
    "$@"
