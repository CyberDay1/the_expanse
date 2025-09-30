#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION="8.10"
DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
CACHE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists"
HASH=$(printf '%s' "$DIST_URL" | md5sum | awk '{print $1}')
INSTALL_BASE="$CACHE_DIR/gradle-${GRADLE_VERSION}/$HASH"
GRADLE_HOME="$INSTALL_BASE/gradle-${GRADLE_VERSION}"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$INSTALL_BASE"
  TMP_ZIP=$(mktemp)
  echo "Downloading Gradle ${GRADLE_VERSION}..." >&2
  curl -fsSL "$DIST_URL" -o "$TMP_ZIP"
  unzip -q "$TMP_ZIP" -d "$INSTALL_BASE"
  rm -f "$TMP_ZIP"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
