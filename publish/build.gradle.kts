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

logger

publishMods {
    val commonDeps = publishConfig.commonDependencies

    if (isGithubWorkflow && publishConfig.artifacts.isEmpty()) {
        println("No artifacts configured")
        exitProcess(1)
    }

    val configuration = PlatformConfiguration(project, this, modVersion, publishConfig, logger)

    val cfConfigurer = CurseforgeConfigurer(configuration, curseforgeToken)
    val mrConfigurer = ModrinthConfigurer(configuration, modrinthToken)

    dryRun.set(dryRunEnabled)

    type.set(STABLE)
    changelog.set(changelogFile.readText())
    version.set(modVersion)
    displayName.set("v${modVersion}")

    publishConfig.artifacts.forEach { artifact ->
        if (publishConfig.curseforgeEnabled) {
            cfConfigurer.configure(artifact)
        }
        if (publishConfig.modrinthEnabled) {
            mrConfigurer.configure(artifact)
        }
        if (publishConfig.discordEnabled && !discordBranchMissing && publishConfig.discordBranch.equals(artifact.branch)) {
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