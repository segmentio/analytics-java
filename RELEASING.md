Releasing
========

 1. Verify the build: `mvn clean verify`.
 2. Update the `CHANGELOG.md` for the impending release.
 3. `mvn clean release:clean`
 4. `mvn release:prepare release:perform`
 5. Visit [Sonatype Nexus](https://oss.sonatype.org/) and promote the artifact.
