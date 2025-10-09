# Build Security

## Dependency verification workflow

Gradle dependency verification is enabled for this project. All CI builds run with `--verify`, which instructs Gradle to compare downloaded artifacts with the checksums stored in `gradle/verification-metadata.xml`. If a dependency is tampered with or the checksum is missing, the build will fail.

When dependencies are upgraded or new ones are introduced, regenerate the metadata locally:

```bash
./gradlew --write-verification-metadata sha256 help
```

The `sha256` algorithm is required so the generated file contains strong checksums. Running the command updates `gradle/verification-metadata.xml` with the new artifact information. Commit the refreshed file as part of the dependency upgrade.

## Review policy

* Never edit `gradle/verification-metadata.xml` by hand. Always regenerate it with the Gradle command above.
* Only update the metadata as part of a reviewed pull request. Include the command output (or a note that it was executed) in the PR description so reviewers know why the file changed.
* If verification failures occur in CI, run the metadata generation command locally to refresh checksums, investigate why the dependency changed, and open a PR with the updated file once validated.

Following this process ensures the build fails when dependencies do not match the expected checksums and that any changes to trusted artifacts receive peer review.
