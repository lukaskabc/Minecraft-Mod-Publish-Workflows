import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.DeserializationFeature
import tools.jackson.module.kotlin.kotlinModule
import kotlin.system.exitProcess

// ENV values
val CURSEFORGE_API_KEY = "CURSEFORGE_API_KEY"
val MODRINTH_API_KEY = "MODRINTH_API_KEY"
val DISCORD_WEBHOOK_URL = "DISCORD_WEBHOOK_URL"

plugins {
    id("me.modmuss50.mod-publish-plugin") // version defined in buildSrc
}

val isGithubWorkflow = System.getenv("GITHUB_ACTIONS") == "true"

/**
 * Reflects the `DRY_RUN` env variable.
 * Implicitly `true` when not running in github workflow.
 */
val dryRunEnabled = providers.environmentVariable("DRY_RUN").orNull == "true" || !isGithubWorkflow

val mapper: JsonMapper = JsonMapper.builder()
    .addModule(kotlinModule())
    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .build()

val changelogFile = file("changelog.md")
val modVersion = providers.environmentVariable("VERSION").orElse("0.0.1").get()

val publishConfig: ArtifactsSchema = mapper.readValue(
    providers.environmentVariable("ARTIFACTS_JSON").orElse("{}").get(),
    ArtifactsSchema::class.java
)

val curseforgeToken = providers.environmentVariable(CURSEFORGE_API_KEY).orElse(provider {
    if (publishConfig.curseforgeEnabled) {
        throw Exception("Environment variable $CURSEFORGE_API_KEY is not set")
    }
    return@provider "Curseforge disabled"
}).get()

val modrinthToken = providers.environmentVariable(MODRINTH_API_KEY).orElse(provider {
    if (publishConfig.modrinthEnabled) {
        throw Exception("Environment variable $MODRINTH_API_KEY is not set")
    }
    return@provider "Modrinth disabled"
}).get()

val discordWebhookUrl = providers.environmentVariable(DISCORD_WEBHOOK_URL).orElse(provider {
    if (publishConfig.discordEnabled) {
        throw Exception("Environment variable $DISCORD_WEBHOOK_URL is not set")
    }
    return@provider "Discord disabled"
}).get()


val discordBranchMissing = publishConfig.discordEnabled && publishConfig.artifacts.stream()
    .filter { it.branch.equals(publishConfig.discordBranch) }
    .findAny()
    .isEmpty
if (discordBranchMissing) {
    logger.warn("Skipping discord publication, discord branch '${publishConfig.discordBranch}' is not being published!")
}



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
    val commonDeps = publishConfig.commonDependencies

    if (isGithubWorkflow && publishConfig.artifacts.isEmpty()) {
        println("No artifacts configured")
        exitProcess(1)
    }

    fun configureCurseforge(artifact: Artifact) {
        curseforge("curseforge-${artifact.branch}") {
            fun configureDependency(dep: CfDependency) {
                val depType = ConfigHelpers.mapDependencyType(dep.dependencyType)
                addInternal(depType) {
                    slug = dep.slug
                }
            }

            accessToken.set(curseforgeToken)

            // the artifact to upload
            file.set(artifactFile(artifact))

            artifact.loaders.forEach {
                modLoaders.add(it.name.toDefaultLowerCase())
            }
            minecraftVersions.addAll(artifact.gameVersions)

            commonDeps.curseforge.forEach(::configureDependency)
            artifact.dependencies.curseforge.forEach(::configureDependency)

            client.set(publishConfig.client)
            server.set(publishConfig.server)

            projectSlug.set(publishConfig.curseforgeProjectSlug)
            projectId.set(publishConfig.curseforgeProjectId.toString())
            changelogType.set("markdown")

            javaVersions.add(JavaVersion.toVersion(artifact.javaVersion))
        }
    }

    fun configureModrinth(artifact: Artifact) {
        modrinth("modrinth-${artifact.branch}") {
            fun configureDependency(dep: MrDependency) {
                val depType = ConfigHelpers.mapDependencyType(dep.dependencyType)
                addInternal(depType) {
                    if (dep.id != null) {
                        id.set(dep.id.toString())
                    } else if (dep.slug != null) {
                        slug.set(dep.slug)
                    }
                }
            }

            accessToken.set(modrinthToken)

            // the artifact to upload
            file.set(artifactFile(artifact))

            artifact.loaders.forEach {
                modLoaders.add(it.name.toDefaultLowerCase())
            }
            minecraftVersions.addAll(artifact.gameVersions)

            commonDeps.modrinth.forEach(::configureDependency)
            artifact.dependencies.modrinth.forEach(::configureDependency)

            val clientBit = if(publishConfig.client) 1 else 0
            val serverBit = if(publishConfig.server) 2 else 0

            val env: ModrinthEnvironment? = when (clientBit or serverBit) {
                1 -> CLIENT_ONLY
                2 -> SERVER_ONLY
                3 -> CLIENT_AND_SERVER
                else -> {
                    logger.error("Unexpected side configuration: server ${publishConfig.server} | client ${publishConfig.client}")
                    null
                }
            }

            if (env != null) {
                environment.set(env)
            }

            projectId.set(publishConfig.modrinthProjectId.toString())
        }
    }

    dryRun.set(dryRunEnabled)

    type.set(STABLE)
    changelog.set(changelogFile.readText())
    version.set(modVersion)
    displayName.set("v${modVersion}")

    publishConfig.artifacts.forEach { artifact ->
        if (publishConfig.curseforgeEnabled) {
            configureCurseforge(artifact)
        }
        if (publishConfig.modrinthEnabled) {
            configureModrinth(artifact)
        }
        if (publishConfig.discordEnabled && !discordBranchMissing) {
            discord {
                webhookUrl.set(discordWebhookUrl)
                username.set("Test username")
                content.set(changelog.map { "# v${modVersion} released!\n" + it }.get())
                style {
                    look.set("MODERN")
                    link.set("BUTTON")
                }
            }
        }
    }

}