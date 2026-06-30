import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.DeserializationFeature
import tools.jackson.module.kotlin.kotlinModule
import kotlin.system.exitProcess

plugins {
    id("me.modmuss50.mod-publish-plugin") // version defined in buildSrc
}

val envProvider = ConfigHelpers.createEnvProvider(providers)

val mapper: JsonMapper = JsonMapper.builder()
    .addModule(kotlinModule())
    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .build()

val changelogFile = file("changelog.md")

val publishConfig: ArtifactsSchema = mapper.readValue(
    envProvider.publishConfig(),
    ArtifactsSchema::class.java
)

val discordBranchMissing = publishConfig.discordEnabled && publishConfig.artifacts.stream()
    .filter { it.branch.equals(publishConfig.discordBranch) }
    .findAny()
    .isEmpty
if (discordBranchMissing) {
    logger.warn("Skipping discord publication, discord branch '${publishConfig.discordBranch}' is not being published!")
}

publishMods {
    if (envProvider.isGithubWorkflow() && publishConfig.artifacts.isEmpty()) {
        println("No artifacts configured")
        exitProcess(1)
    }

    val modVersion = envProvider.modVersion();
    val configuration = PlatformConfiguration(project, this, modVersion, publishConfig, logger)

    val cfConfigurer = CurseforgeConfigurer(configuration, provider<String>(envProvider::curseforgeToken))
    val mrConfigurer = ModrinthConfigurer(configuration, provider<String>(envProvider::modrinthToken))

    dryRun.set(envProvider.isDryRun())

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
                webhookUrl.set(envProvider.discordWebhookUrl())
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