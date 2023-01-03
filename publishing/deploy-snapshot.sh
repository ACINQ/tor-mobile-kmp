#!/bin/bash -x

# This script deploys several artifacts of the tor-mobile-kmp lib to the SNAPSHOT
# sonatype maven repository.

# See the README.md file in this folder for usage instructions.

GROUP_ID=fr.acinq.tor
ARTIFACT_ID=tor-mobile-kmp

read -p 'Enter the release version: ' VERSION

if [[ -z "$VERSION" ]]; then
  echo "Error: version is not valid." >&2
  exit
fi

# This folder is created by the `archive` Github action, which must be started manually.
cd tor-mobile-kmp-archive
pushd .

# Deploy base artifact
cd fr/acinq/tor/$ARTIFACT_ID/$VERSION
mvn deploy:deploy-file -DrepositoryId=ossrh -Durl=https://oss.sonatype.org/content/repositories/snapshots/ \
    -DpomFile=$ARTIFACT_ID-$VERSION.pom \
    -Dfile=$ARTIFACT_ID-$VERSION.jar \
    -Dfiles=$ARTIFACT_ID-$VERSION.module,$ARTIFACT_ID-$VERSION-kotlin-tooling-metadata.json \
    -Dtypes=module,json \
    -Dclassifiers=,kotlin-tooling-metadata \
    -Dsources=$ARTIFACT_ID-$VERSION-sources.jar \
    -Djavadoc=$ARTIFACT_ID-$VERSION-javadoc.jar

popd
pushd .

# Deploy variants
for VARIANT in android iosarm64 iosx64
do
  cd fr/acinq/tor/$ARTIFACT_ID-$VARIANT/$VERSION
  if [ $VARIANT == iosarm64 ] || [ $VARIANT == iosx64 ]; then
    mvn deploy:deploy-file -DrepositoryId=ossrh -Durl=https://oss.sonatype.org/content/repositories/snapshots/ \
      -DpomFile=r-$VARIANT-$VERSION.pom \
      -Dfile=$ARTIFACT_ID-$VARIANT-$VERSION.klib \
      -Dfiles=$ARTIFACT_ID-$VARIANT-$VERSION.module,$ARTIFACT_ID-$VARIANT-$VERSION-cinterop-libsecp256k1.klib  \
      -Dtypes=module,klib \
      -Dclassifiers=,cinterop-libsecp256k1 \
      -Dsources=$ARTIFACT_ID-$VARIANT-$VERSION-sources.jar \
      -Djavadoc=$ARTIFACT_ID-$VARIANT-$VERSION-javadoc.jar
  elif [ $VARIANT == android ]; then
    mvn deploy:deploy-file -DrepositoryId=ossrh -Durl=https://oss.sonatype.org/content/repositories/snapshots/ \
      -DpomFile=$ARTIFACT_ID-$VARIANT-$VERSION.pom \
      -Dfile=$ARTIFACT_ID-$VARIANT-$VERSION.aar \
      -Dfiles=$ARTIFACT_ID-$VARIANT-$VERSION.module \
      -Dtypes=module \
      -Dclassifiers= \
      -Dsources=$ARTIFACT_ID-$VARIANT-$VERSION-sources.jar \
      -Djavadoc=$ARTIFACT_ID-$VARIANT-$VERSION-javadoc.jar
  fi
  popd
  pushd .
done