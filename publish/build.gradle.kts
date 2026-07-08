import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.DeserializationFeature
import tools.jackson.module.kotlin.kotlinModule
import kotlin.system.exitProcess
import cz.lukaskabc.minecraft.ci.publish.*
import cz.lukaskabc.minecraft.ci.publish.action.*
import cz.lukaskabc.minecraft.ci.publish.schema.*
import cz.lukaskabc.minecraft.ci.publish.task.*

plugins {
    id("me.modmuss50.mod-publish-plugin") // version defined in buildSrc
}

logger.lifecycle("Initializing release configuration")

fun createEnvProvider(providers: ProviderFactory): EnvProvider {
    val envProvider = EnvProvider(providers)
    if (envProvider.isGithubWorkflow())
        return envProvider
    return StubEnvProvider(project)
}

val envProvider = createEnvProvider(providers)
val execTask = envProvider.execTask()

logger.lifecycle("Selected execution task ${execTask.name}")

val mapper: JsonMapper = JsonMapper.builder()
    .addModule(kotlinModule())
    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .build()

val publishConfig: PublishConfigSchema = mapper.readValue(
    envProvider.publishConfig(),
    PublishConfigSchema::class.java
)

JakartaValidator.validate(publishConfig)

publishMods {
    if (envProvider.isGithubWorkflow() && publishConfig.artifacts.isEmpty()) {
        println("No artifacts configured")
        exitProcess(1)
    }

    val modVersion = envProvider.modVersion();
    val configuration = ProjectConfiguration(project, this, modVersion, publishConfig, logger, envProvider)

    val actionProvider: ActionProvider = when (execTask) {
        ExecTask.PUBLISH_MODS -> PublishReleaseAction(configuration)
        ExecTask.NIGHTLY_DISCORD_ANNOUNCE -> DiscordNightlyAnnounceAction(configuration)
        /* Handled by a standalone task */
        ExecTask.DISCORD_NIGHTLY_FILE_UPLOAD -> NoOpActionProvider()
        else -> {
            throw GradleException("Unknown execution task: ${execTask.name}")
        }
    }

    actionProvider.get().execute(this)
}

tasks.register<DiscordNightlyFileUploadTask>("uploadDiscordNightlyFile") {
    discordEnabled = publishConfig.discordAnnounceNightlyBuilds
    val artifactDirPath = envProvider.nightlyArtifactDir()
    artifactsDir = project.layout.projectDirectory.dir(artifactDirPath)
    webhookUrl = envProvider.discordNightlyWebhookUrl()
    webhookConfig = publishConfig.discordWebhook
}