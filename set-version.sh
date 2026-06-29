#!/usr/bin/env bash
#
# Sets a version value in one or more "key=value" property files, then commits
# the changes (if any).
#
# Usage:
#   set-version.sh <version> <version_properties_json> <commit_message>
#
# Arguments:
#   version                   The version string to write, e.g. "1.2.3".
#   version_properties_json   JSON array of {"file":"...","property":"..."}.
#                               If empty or "[]", defaults to:
#                               [{"file":"gradle.properties","property":"mod_version"}]
#   commit_message            Commit message to use.
#
# Example:
#   ./set-version.sh "1.2.3" \
#       '[{"file":"gradle.properties","property":"mod_version"}]' \
#       "[chore(release)] set version to 1.2.3"

set -euo pipefail

usage() {
  echo "Usage: $0 <version> <version_properties_json> <commit_message>" >&2
  exit 1
}

if [[ $# -ne 3 ]]; then
  usage
fi

VERSION="$1"
SPEC_JSON_INPUT="$2"
COMMIT_MESSAGE="$3"

if [[ -z "$VERSION" ]]; then
  echo "::error::version argument must not be empty" >&2
  exit 1
fi

DEFAULT_SPEC='[{"file":"gradle.properties","property":"mod_version"}]'
SPEC_JSON="${SPEC_JSON_INPUT:-}"

if [[ -z "$SPEC_JSON" || "$SPEC_JSON" == "[]" ]]; then
  SPEC_JSON="$DEFAULT_SPEC"
fi

# Array to track exactly which files we modified
declare -a modified_files=()

while IFS= read -r row; do
  file=$(jq -r '.file' <<<"$row")
  prop=$(jq -r '.property' <<<"$row")

  if [[ ! -f "$file" ]]; then
    echo "::error::Missing file: $file"
    exit 1
  fi

  awk -v prop="$prop" -v ver="$VERSION" '
    {
      if (index($0, prop "=") == 1) {
        print prop "=" ver
        found = 1
      } else {
        print
      }
    }
    END {
      if (!found) print prop "=" ver
    }
  ' "$file" > "${file}.tmp" && mv "${file}.tmp" "$file"

  modified_files+=("$file")
  echo "Set ${prop}=${VERSION} in ${file}"
done < <(jq -c '.[]' <<<"$SPEC_JSON")

# Only attempt Git operations if changes actually occurred
if ! git diff --quiet -- "${modified_files[@]}"; then
  git add -- "${modified_files[@]}"
  git commit -m "$COMMIT_MESSAGE"
else
  echo "No changes to commit for release version (${#modified_files[@]} target(s) processed)."
fi