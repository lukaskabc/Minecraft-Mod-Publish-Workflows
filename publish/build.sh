set -euo pipefail

export CONFIG_JSON=$(cat testing.publish.config.json)
export VERSION=0.5.2
export CURSEFORGE_API_KEY=cf_key
export MODRINTH_API_KEY=mr_key
export DISCORD_WEBHOOK_URL=http://disabled
export DRY_RUN=false
export GITHUB_ACTIONS=true

export PLATFORM=curseforge
export ARTIFACT_ID=SOME-ID-main
#./gradlew :buildSrc:clean
./gradlew publishArtifact