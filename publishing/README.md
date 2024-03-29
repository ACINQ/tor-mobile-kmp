# Publishing tor-mobile-kmp artifacts

## Snapshot artifacts

Snapshots are published to the Sonatype snapshot repository (https://oss.sonatype.org/content/repositories/snapshots/).

### Requirements

You must add your sonatype credentials for the `ossrh` server to your local maven settings (typically in `$HOME/.m2/settings.xml`)

### Usage

1. Run the `archive` action on GitHub;
2. Download the zip file generated by the action above;
3. Unzip the file to the `publishing` directory;
4. Run `deploy-snapshot.sh`. The script will prompt for a version: enter the current project version, which should be a `SNAPSHOT`.

## Release artifacts

Releases are published to the Sonatype staging repository. If all items are valid, they will be published to the `maven central` repository.

### Requirements

You must have a valid GPG key. Sonatype credentials must be in the environment variables: `$SONATYPE_USER` and `$SONATYPE_PASS`.

### Usage

1. Run the `archive` action on GitHub;
2. Download the zip file generated by the action above;
3. Unzip the file to the `publishing` directory;
4. Sign all artifacts with a valid gpg key: 
```shell
find release -type f -print -exec gpg -ab {} \;
```
5. Run `deploy-staging.sh`; the script will prompt for a version: enter the current project version, which should be a tag without `SNAPSHOT`.
6. Log into Sonatype, and close and publish your staging repository.

Artifacts will be available on Maven Central within a few hours.
