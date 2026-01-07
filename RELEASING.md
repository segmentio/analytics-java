Releasing
=========

 1. Verify the build: `mvn clean verify`.
 2. Update the `CHANGELOG.md` for the impending release.
 3. Deploy the release artifacts to Sonatype OSSRH using the `central-publishing-maven-plugin` (configured in the parent `pom.xml`).
 4. Visit the [Maven Central Portal](https://central.sonatype.com/) to review and publish the release.
