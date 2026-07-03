#!/usr/bin/env bash

# For a plain release (e.g. 1.2.3) the patch is bumped and a new "-alpha" dev cycle starts (e.g. 1.2.4-alpha).
# For a hotfix/prerelease release (e.g. 1.2.3-hotfix1, 1.2.3-hotfix.1, 1.2.3-rc.2)
# the major.minor.patch is left untouched and only the trailing number of the prerelease identifier is bumped instead
# (e.g. 1.2.3-hotfix1 -> 1.2.3-hotfix2).

set -euo pipefail

core="${INPUT_VERSION%%[-+]*}"
rest="${INPUT_VERSION#"$core"}"
IFS='.' read -r major minor patch <<< "$core"

prerelease=""
if [[ "$rest" == -* ]]; then
  rest="${rest#-}"          # drop leading '-'
  prerelease="${rest%%+*}"  # drop build metadata after '+', if any
fi

if [[ -z "$prerelease" ]]; then
  # Plain release: bump the patch and start a fresh alpha cycle
  next_patch=$(( 10#$patch + 1 ))
  next_dev="${major}.${minor}.${next_patch}-alpha"
else
  # Hotfix/prerelease release: stay on the same major.minor.patch
  # and bump the prerelease's own trailing counter instead
  if [[ "$prerelease" =~ ^(.*[^0-9])([0-9]+)$ ]]; then
    prefix="${BASH_REMATCH[1]}"
    num="${BASH_REMATCH[2]}"
    next_prerelease="${prefix}$(( 10#$num + 1 ))"
  elif [[ "$prerelease" =~ ^[0-9]+$ ]]; then
    next_prerelease=$(( 10#$prerelease + 1 ))
  else
    # no numeric part to bump (e.g. "alpha") -> start a counter
    next_prerelease="${prerelease}.1"
  fi
  next_dev="${major}.${minor}.${patch}-${next_prerelease}-alpha"
fi

echo $next_dev