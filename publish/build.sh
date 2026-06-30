export ARTIFACTS_JSON=$(cat testing.publish.config.json)
export VERSION=0.1.2
export DRY_RUN=true
./gradlew --info :buildSrc:clean
./gradlew --info :buildSrc:build
./gradlew --info tasks