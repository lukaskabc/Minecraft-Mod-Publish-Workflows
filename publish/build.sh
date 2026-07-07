set -euo pipefail

export ARTIFACTS_JSON=$(cat testing.publish.config.json)
export VERSION=0.5.2
export CURSEFORGE_API_KEY=cf_key
export MODRINTH_API_KEY=mr_key
export DISCORD_WEBHOOK_URL=http://disabled
export DRY_RUN=true
./gradlew :buildSrc:clean
./gradlew tasks