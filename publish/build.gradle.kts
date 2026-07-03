import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.DeserializationFeature
import tools.jackson.module.kotlin.kotlinModule
import kotlin.system.exitProcess

plugins {
    id("me.modmuss50.mod-publish-plugin") // version defined in buildSrc
}

logger.lifecycle("Initializing release configuration")

fun createEnvProvider(providers: ProviderFactory): EnvProvider {
    val envProvider = EnvProvider(providers)
    if (envProvider.isGithubWorkflow())
        return envProvider
    return StubEnvProvider(providers)
}

val envProvider = createEnvProvider(providers)

val mapper: JsonMapper = JsonMapper.builder()
    .addModule(kotlinModule())
    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .build()

val changelogFile = file("changelog.md")

val publishConfig: PublishConfigSchema = mapper.readValue(
    envProvider.publishConfig(),
    PublishConfigSchema::class.java
)

/**
 * When `true` the discord announcement is enabled
 * but the configured discord branch is not being published during this run.
 */
val discordBranchMissing = publishConfig.discordEnabled
        && publishConfig.artifacts
    .none { it.branch == publishConfig.discordBranch }

if (discordBranchMissing) {
    throw Exception("Unable to configure Discord publication, discord branch '${publishConfig.discordBranch}' is not being published!")
}

publishMods {
    logger.lifecycle("Configuring mod publishing")

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
        logger.lifecycle("Configuring release for branch ${artifact.branch}")
        if (publishConfig.curseforgeEnabled) {
            logger.lifecycle("- curseforge")
            cfConfigurer.configure(artifact)
        }
        if (publishConfig.modrinthEnabled) {
            logger.lifecycle("- modrinth")
            mrConfigurer.configure(artifact)
        }
        if (publishConfig.discordEnabled && publishConfig.discordBranch == artifact.branch) {
            logger.lifecycle("- discord")
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