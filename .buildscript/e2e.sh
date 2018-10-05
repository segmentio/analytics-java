#!/bin/sh

set -e

if [ "$RUN_E2E_TESTS" != "true" ]; then
  echo "Skipping end to end tests."
else
  echo "Running end to end tests..."
  exe="tester_linux_amd64"
  rm -f ${exe}
  wget -O - https://raw.githubusercontent.com/segmentio/library-e2e-tester/master/.buildscript/get-latest-version.sh | bash -s ${exe}
  chmod +x ${exe}
  ./${exe} -segment-write-key="$SEGMENT_WRITE_KEY" -webhook-auth-username="$WEBHOOK_AUTH_USERNAME" -webhook-bucket="$WEBHOOK_BUCKET" -path='.buildscript/cli.sh'
  echo "End to end tests completed!"
fi
