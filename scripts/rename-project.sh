#!/usr/bin/env bash
# Rename the template for a new client project.
#
#   ./scripts/rename-project.sh <groupId> <artifactId> [basePackage]
#
# Example:
#   ./scripts/rename-project.sh com.acme inventory-api com.acme.inventory
#
# - pom.xml groupId / artifactId / name
# - Java package com.example.starter -> <basePackage> (moves the folders too)
# - StarterApplication -> <Artifact>Application
# - application name, JWT issuer, docker compose project, log levels
set -euo pipefail

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <groupId> <artifactId> [basePackage]" >&2
  exit 1
fi

GROUP_ID="$1"
ARTIFACT_ID="$2"
BASE_PACKAGE="${3:-${GROUP_ID}.$(echo "$ARTIFACT_ID" | tr -cd '[:alnum:]' | tr '[:upper:]' '[:lower:]')}"

OLD_PACKAGE="com.example.starter"
OLD_ARTIFACT="springboot-starter"

if [[ ! "$BASE_PACKAGE" =~ ^[a-z_][a-z0-9_]*(\.[a-z_][a-z0-9_]*)*$ ]]; then
  echo "Invalid Java package: $BASE_PACKAGE" >&2
  exit 1
fi

# CamelCase class prefix from artifactId: inventory-api -> InventoryApi
CLASS_PREFIX="$(echo "$ARTIFACT_ID" | sed -E 's/(^|[-_ .])([a-zA-Z0-9])/\U\2/g' | tr -cd '[:alnum:]')"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# sed -i portable between GNU and BSD
sedi() { if sed --version >/dev/null 2>&1; then sed -i "$@"; else sed -i '' "$@"; fi; }

echo "groupId     : $GROUP_ID"
echo "artifactId  : $ARTIFACT_ID"
echo "package     : $BASE_PACKAGE"
echo "main class  : ${CLASS_PREFIX}Application"

# 1. pom.xml
sedi -E "0,/<groupId>com\.example<\/groupId>/s//<groupId>${GROUP_ID}<\/groupId>/" pom.xml
sedi -E "s/<artifactId>${OLD_ARTIFACT}<\/artifactId>/<artifactId>${ARTIFACT_ID}<\/artifactId>/" pom.xml
sedi -E "s/<name>${OLD_ARTIFACT}<\/name>/<name>${ARTIFACT_ID}<\/name>/" pom.xml

# 2. Move Java sources
OLD_PATH="${OLD_PACKAGE//.//}"
NEW_PATH="${BASE_PACKAGE//.//}"
for src in src/main/java src/test/java; do
  if [[ -d "$src/$OLD_PATH" && "$OLD_PATH" != "$NEW_PATH" ]]; then
    mkdir -p "$src/$NEW_PATH"
    cp -R "$src/$OLD_PATH/." "$src/$NEW_PATH/"
    rm -rf "$src/$OLD_PATH"
    # remove now-empty parent folders (com/example)
    find "$src" -type d -empty -delete
  fi
done

# 3. Replace package names & artifact name in text files
FILES=$(grep -rlI -e "$OLD_PACKAGE" -e "$OLD_ARTIFACT" \
  --exclude-dir=.git --exclude-dir=target --exclude-dir=uploads --exclude=rename-project.sh . || true)
for f in $FILES; do
  sedi -e "s/${OLD_PACKAGE//./\\.}/${BASE_PACKAGE}/g" -e "s/${OLD_ARTIFACT}/${ARTIFACT_ID}/g" "$f"
done

# 4. Rename application classes
for src in src/main/java src/test/java; do
  for cls in StarterApplication TestStarterApplication; do
    file="$src/$NEW_PATH/$cls.java"
    if [[ -f "$file" ]]; then
      new="${cls/Starter/$CLASS_PREFIX}"
      mv "$file" "$src/$NEW_PATH/$new.java"
    fi
  done
done
FILES=$(grep -rlI "StarterApplication" --exclude-dir=.git --exclude-dir=target --exclude=rename-project.sh . || true)
for f in $FILES; do
  sedi -e "s/StarterApplication/${CLASS_PREFIX}Application/g" "$f"
done

echo
echo "Done. Next steps:"
echo "  1. cp .env.example .env  &&  set JWT_SECRET (openssl rand -base64 48)"
echo "  2. ./mvnw verify"
echo "  3. git init (or remove the template history) and commit"
