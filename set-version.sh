#!/usr/bin/env bash
#
# Sets a version value in one or more "key=value" property files, then commits
# the changes (if any).
#
# If a commit with the exact given commit_message already exists in the current branch's history, this script
# assumes that bump was already performed (e.g. by an earlier, possibly re-run, CI attempt)
# and reuses that commit instead of creating a duplicate one.
#
# Usage:
#   set-version.sh <version> <version_properties_json> <commit_message>
#
# Arguments:
#   version                   The version string to write, e.g. "1.2.3".
#   version_properties_json   JSON array of {"file":"...","property":"..."}.
#                               If empty or "[]", defaults to:
#                               [{"file":"gradle.properties","property":"mod_version"}]
#   commit_message            Commit message to use. Must be unique per distinct version bump.
#                               Used for deduplication.
#
# Output:
#   Writes the resulting commit hash (existing or newly created) to the GitHub Actions
#   $GITHUB_OUTPUT environment file under the key 'commit_hash'.
#

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

if [[ -z "${GITHUB_OUTPUT:-}" ]]; then
  echo "::error::GITHUB_OUTPUT environment variable is not set. Are you running this in GitHub Actions?" >&2
  exit 1
fi

# If a commit with this exact message already exists in the current branch's history,
# this exact bump was already performed.
# Reuse it instead of touching any files or creating a duplicate commit.
EXISTING_HASH="$(git log --fixed-strings --grep="$COMMIT_MESSAGE" --format=%H -n 1 || true)"
if [[ -n "$EXISTING_HASH" ]]; then
  echo "Commit '$COMMIT_MESSAGE' already exists ($EXISTING_HASH), reusing it." >&2
  echo "commit_hash=$EXISTING_HASH" >> "$GITHUB_OUTPUT"
  exit 0
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
    echo "::error::Missing file: $file" >&2
    exit 1
  fi

  awk -v prop="$prop" -v ver="$VERSION" '
    BEGIN {
      # Safely escape dots in case the property is something like "mod.version"
      safe_prop = prop
      gsub(/\./, "\\.", safe_prop)

      # Match leading spaces, property name, spaces, equals, and trailing spaces
      pattern = "^[[:space:]]*" safe_prop "[[:space:]]*=[[:space:]]*"
    }
    match($0, pattern) {
      # Extract the exact matched string (everything up to where the value starts)
      prefix = substr($0, 1, RLENGTH)
      print prefix ver
      found = 1
      next
    }
    {
      print
    }
    END {
      if (!found) print prop "=" ver
    }
  ' "$file" > "${file}.tmp" && mv "${file}.tmp" "$file"

  modified_files+=("$file")
  echo "Set ${prop}=${VERSION} in ${file}"
done < <(jq -c '.[]' <<<"$SPEC_JSON")

# Only attempt Git operations if changes actually occurred
if ! git diff --quiet "${modified_files[@]}"; then
  git add "${modified_files[@]}"
  git commit -m "$COMMIT_MESSAGE"
else
  echo "No changes to commit for release version (${#modified_files[@]} target(s) processed)."
fi

echo "commit_hash=$(git rev-parse HEAD)" >> "$GITHUB_OUTPUT"