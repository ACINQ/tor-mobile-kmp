#!/bin/bash -x

# This script deploys several artifacts of the tor-mobile-kmp lib to the STAGING
# Sonatype maven repository. If valid, they will be published to maven central.

# All files must be signed! See the README in this folder for instructions.

GROUP_ID=fr.acinq.tor
ARTIFACT_ID=tor-mobile-kmp

read -p 'Enter the release version: ' VERSION

if [[ -z "$VERSION" || "$VERSION" =~ snapshot|SNAPSHOT ]]; then
  echo "Error: version is not valid." >&2
  exit
elif [[ -z "$SONATYPE_USER" ]]; then
  echo "Error: credentials missing! Please define a valid SONATYPE_USER environment variable." >&2
  exit
elif [[ -z "$SONATYPE_PASS" ]]; then
  echo "Error: credentials missing! Please define a valid SONATYPE_PASS environment variable." >&2
  exit
else
  for VARIANT in $ARTIFACT_ID $ARTIFACT_ID-android $ARTIFACT_ID-iosarm64 $ARTIFACT_ID-iosx64
  do
  	pushd .
  	cd tor-mobile-kmp-archive/fr/acinq/secp256k1/$VARIANT/$VERSION
  	pwd
  	jar -cvf bundle.jar *
    curl -v -XPOST -u $SONATYPE_USER:$SONATYPE_PASS --upload-file bundle.jar https://oss.sonatype.org/service/local/staging/bundle_upload
    popd
  done
fi

