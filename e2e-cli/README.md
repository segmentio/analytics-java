# analytics-java e2e-cli

E2E test CLI for the [analytics-java](https://github.com/segmentio/analytics-java) SDK. Accepts a JSON input describing events and SDK configuration, sends them through the real SDK, and outputs results as JSON.

Built with Kotlin (JVM) and packaged as a fat jar via Maven.

## Running E2E tests

### With devbox (recommended)

```bash
# From repo root — activates Java 11 and Maven automatically
devbox shell

# Then from e2e-cli dir:
./run-e2e.sh
```

### Without devbox

Requires Java 11+, Maven, and Node.js 18+.

```bash
./run-e2e.sh
```

The script auto-detects `java` and `mvn` on PATH. If they're not on PATH but devbox has been initialized, it falls back to the devbox nix profile binaries automatically.

### Override sdk-e2e-tests location

```bash
E2E_TESTS_DIR=../my-e2e-tests ./run-e2e.sh
```

## Manual CLI usage

```bash
# Build first (from repo root)
mvn package -pl e2e-cli -am -DskipTests

# Run
java -jar e2e-cli/target/e2e-cli-*-jar-with-dependencies.jar --input '{"writeKey":"...", ...}'
```

## Input Format

```jsonc
{
  "writeKey": "your-write-key",       // required
  "apiHost": "https://...",           // optional — SDK default if omitted
  "sequences": [                      // required — event sequences to send
    {
      "delayMs": 0,
      "events": [
        { "type": "track", "event": "Test", "userId": "user-1" }
      ]
    }
  ],
  "config": {                         // optional
    "flushAt": 250,
    "flushInterval": 10000,
    "maxRetries": 3,
    "timeout": 15
  }
}
```

Note: Java is a server-side SDK — there is no CDN settings fetch, so `cdnHost` does not apply.

## Output Format

```json
{ "success": true, "sentBatches": 1 }
```

On failure:

```json
{ "success": false, "error": "description", "sentBatches": 0 }
```
