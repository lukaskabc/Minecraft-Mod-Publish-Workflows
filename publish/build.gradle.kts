import me.modmuss50.mpp.PlatformDependency
import me.modmuss50.mpp.platforms.curseforge.CurseforgeDependency
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.kotlinModule
import kotlin.system.exitProcess

plugins {
    id("me.modmuss50.mod-publish-plugin") // version defined in buildSrc
}

val mapper = JsonMapper.builder()
    .addModule(kotlinModule())
    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .build()

val changelogFile = file("changelog.md")
val modVersion = providers.environmentVariable("VERSION").orElse("0.0.1").get()
val isDryRun = providers.environmentVariable("DRY_RUN").orNull == "true"

val publishConfig: ArtifactsSchema = mapper.readValue(
    providers.environmentVariable("ARTIFACTS_JSON").orElse("{}").get(),
    ArtifactsSchema::class.java
)

/**
 * Resolves an artifact jar file from `./artifacts` directory
 */
fun artifactFile(artifact: Artifact): File {
    val glob = ConfigHelpers.jarNameGlob(artifact, modVersion)
    val artifacts = fileTree("./artifacts") {
        include(glob)
    }
    return artifacts.files.firstOrNull() ?: throw Exception("Failed to match artifact file for $glob")
}


publishMods {
    val common_deps = publishConfig.commonDependencies

//    if (publishConfig.artifacts.isEmpty()) {
//        println("No artifacts configured")
//        exitProcess(1)
//    }

    fun configureCurseforge(artifact: Artifact) {
        curseforge("curseforge-${artifact.branch}") {
            // the artifact to upload
            file.set(artifactFile(artifact))

            artifact.loaders.forEach {
                modLoaders.add(it.name.toDefaultLowerCase())
            }
            minecraftVersions.addAll(artifact.gameVersions)

            fun configureDependency(dep: CfDependency) {
                val depType = ConfigHelpers.mapDependencyType(dep.dependencyType)
                addInternal(depType) {
                    slug = dep.slug
                }
            }

            common_deps.curseforge.forEach(::configureDependency)
            artifact.dependencies.curseforge.forEach(::configureDependency)

            client.set(publishConfig.client)
            server.set(publishConfig.server)

            projectSlug.set(publishConfig.curseforgeProjectSlug)
            projectId.set(publishConfig.curseforgeProjectId.toString())
        }
    }

    type.set(STABLE)
    changelog.set(changelogFile.readText())
    version.set(modVersion)
    displayName.set("v${modVersion}")


}