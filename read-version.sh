#!/usr/bin/env bash
#
# Reads the current version value from the first configured property file and prints it to stdout.
#
# If multiple property files are configured, only the first one is consulted
# - all configured files are expected to hold the same version.
#
# Usage:
#   get-version.sh <version_properties_json>
#
# Arguments:
#   version_properties_json   JSON array of {"file":"...","property":"..."}.
#                               If empty, "[]", or "null", defaults to:
#                               [{"file":"gradle.properties","property":"mod_version"}]
#
# Output:
#   Prints the extracted version string to stdout.
#

set -euo pipefail

usage() {
  echo "Usage: $0 <version_properties_json>" >&2
  exit 1
}

if [[ $# -ne 1 ]]; then
  usage
fi

SPEC_JSON_INPUT="$1"
DEFAULT_SPEC='[{"file":"gradle.properties","property":"mod_version"}]'
SPEC_JSON="${SPEC_JSON_INPUT:-}"

if [[ -z "$SPEC_JSON" || "$SPEC_JSON" == "[]" || "$SPEC_JSON" == "null" ]]; then
  SPEC_JSON="$DEFAULT_SPEC"
fi

TARGET_FILE=$(echo "$SPEC_JSON" | jq -r '.[0].file')
TARGET_PROP=$(echo "$SPEC_JSON" | jq -r '.[0].property')

if [[ ! -f "$TARGET_FILE" ]]; then
  echo "::error::Configured version file '$TARGET_FILE' does not exist." >&2
  exit 1
fi

# Extract current version using an awk logic that mirrors set-version.sh's matching
CURRENT_VERSION=$(awk -v prop="$TARGET_PROP" -v dq='"' -v sq="'" '
  BEGIN {
    # Safely escape dots in case the property is something like "mod.version"
    safe_prop = prop
    gsub(/\./, "\\.", safe_prop)

    # Match leading spaces, property name, spaces, equals, and trailing spaces
    pattern = "^[[:space:]]*" safe_prop "[[:space:]]*=[[:space:]]*"
  }
  match($0, pattern) {
    # Extract the value after the matched prefix
    val = substr($0, RLENGTH + 1)
    # Strip trailing whitespace and carriage returns
    sub(/[[:space:]]+$/, "", val)

    # Strip a matching pair of surrounding quotes (single or double)
    n = length(val)
    if (n >= 2) {
      first = substr(val, 1, 1)
      last = substr(val, n, 1)
      if ((first == dq && last == dq) || (first == sq && last == sq)) {
        val = substr(val, 2, n - 2)
      }
    }

    print val
    exit
  }
' "$TARGET_FILE")

if [[ -z "$CURRENT_VERSION" ]]; then
  echo "::error::Failed to extract version for property '$TARGET_PROP' in '$TARGET_FILE'." >&2
  exit 1
fi

echo "$CURRENT_VERSION"