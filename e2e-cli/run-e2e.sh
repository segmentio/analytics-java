#!/bin/bash
#
# Run E2E tests for analytics-java
#
# Prerequisites: Node.js 18+ and one of:
#   - devbox (recommended): run `devbox shell` first, then ./run-e2e.sh
#   - Java 11+ and Maven on PATH
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

# Resolve java and mvn — prefer JAVA_HOME/devbox nix profile, fall back to PATH
if [[ -z "$JAVA" ]]; then
    if command -v java &>/dev/null; then
        JAVA="java"
    elif [[ -f "$SDK_ROOT/.devbox/nix/profile/default/bin/java" ]]; then
        JAVA="$SDK_ROOT/.devbox/nix/profile/default/bin/java"
    else
        echo "Error: java not found. Run 'devbox shell' first or install Java 11+."
        exit 1
    fi
fi

if [[ -z "$MVN" ]]; then
    if command -v mvn &>/dev/null; then
        MVN="mvn"
    elif [[ -f "$SDK_ROOT/.devbox/nix/profile/default/bin/mvn" ]]; then
        MVN="$SDK_ROOT/.devbox/nix/profile/default/bin/mvn"
    else
        echo "Error: mvn not found. Run 'devbox shell' first or install Maven."
        exit 1
    fi
fi

echo "=== Building analytics-java e2e-cli ==="
echo "Using Java: $($JAVA -version 2>&1 | head -1)"
echo "Using Maven: $($MVN -version 2>&1 | head -1)"

# Build SDK and e2e-cli
cd "$SDK_ROOT"
$MVN package -pl e2e-cli -am -DskipTests

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
    --cli "$JAVA -jar $CLI_JAR" \
    "$@"
