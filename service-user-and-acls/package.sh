#!/bin/bash
version="$(grep 'key="version"' ./src/META-INF/vault/properties.xml|sed -e 's/^.*version">//' -e s'/<\/entr.*$//')"
name="$(grep 'key="name"' ./src/META-INF/vault/properties.xml|sed -e 's/^.*name">//' -e s'/<\/entr.*$//')"

fail() {
  echo "$*"
  exit -1
}

# Check
[ -z "$version" ] && fail "Failed to extract package version"
[ -z "$name" ] && fail "Failed to extract package name"

# Clean
rm -fR build

# Build
mkdir -p build || fail "Failed to create build dir"
(cd src && zip --exclude '*/.*.sw[a-z]' -r ../build/$name-$version.zip jcr_root META-INF) || fail "Failed to create AEM package"
