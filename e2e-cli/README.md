# analytics-java e2e-cli

E2E test CLI for the [analytics-java](https://github.com/segmentio/analytics-java) SDK. Accepts a JSON input describing events and SDK configuration, sends them through the real SDK, and outputs results as JSON.

Built with Kotlin (JVM) and packaged as a fat jar via Maven.

## Setup

```bash
mvn package -pl e2e-cli -am
```

## Usage

```bash
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
