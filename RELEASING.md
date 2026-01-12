Releasing
=========

 1. Verify the build: `mvn clean verify`.
 2. Update the `CHANGELOG.md` for the impending release.
 3. `mvn clean release:clean`
 4. `mvn release:prepare release:perform`
 4. Visit the [Maven Central Portal](https://central.sonatype.com/publishing/deployments) to review and publish the release.
