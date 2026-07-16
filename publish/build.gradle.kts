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

val mapper: JsonMapper = JsonMapper.builder()
    .addModule(kotlinModule())
    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .build()

val publishConfig: PublishConfigSchema = mapper.readValue(
    envProvider.publishConfig(),
    PublishConfigSchema::class.java
)

JakartaValidator.validate(publishConfig)

if (envProvider.isGithubWorkflow() && publishConfig.artifacts.additionalProperties.isEmpty()) {
    println("No artifacts configured")
    exitProcess(1)
}

tasks.register(TaskNames.PUBLISH_ARTIFACT.taskName) {
    group = "publishing"
    description = "Publishes a single artifact to the configured platforms"
    dependsOn(TaskNames.PUBLISH_MODS.taskName)
}

tasks.register(TaskNames.ANNOUNCE_DISCORD.taskName) {
    group = "publishing"
    description = "Announces the new release to Discord"
}

tasks.register(TaskNames.NIGHTLY_ANNOUNCE_DISCORD.taskName) {
    group = "publishing"
    description = "Announces and uploads nightly build to Discord"
}

if (gradle.startParameter.taskNames.contains(TaskNames.PUBLISH_MODS.taskName)) {
    throw GradleException("Do not call publishMods directly, choose a single platform and artifact by it's ID to publish")
}

val modVersion = envProvider.modVersion();
val configuration = ProjectConfiguration(project, modVersion, publishConfig, logger, envProvider)
var taskMatched = false

// Iterate tasks from CLI and run respective actions
gradle.startParameter.taskNames.forEach { taskName ->
    val action: Runnable = when (taskName) {
        TaskNames.PUBLISH_ARTIFACT.taskName -> PublishArtifactAction(configuration)
        TaskNames.ANNOUNCE_DISCORD.taskName -> AnnounceDiscordAction(configuration)
        TaskNames.NIGHTLY_ANNOUNCE_DISCORD.taskName -> NightlyAnnounceDiscordAction(configuration)
        else -> return@forEach
    }
    action.run()
    taskMatched = true
}

if (!taskMatched) {
    logger.warn("No Workflow task matched!")
}

//publishMods {
//
//    val modVersion = envProvider.modVersion();
//    val configuration = ProjectConfiguration(project, this, modVersion, publishConfig, logger, envProvider)
//
//    val actionProvider: ActionProvider = when (execTask) {
//        ExecTask.PUBLISH_MODS -> PublishReleaseAction(configuration)
//        ExecTask.NIGHTLY_DISCORD_ANNOUNCE -> DiscordNightlyAnnounceAction(configuration)
//        /* Handled by a standalone task */
//        ExecTask.DISCORD_NIGHTLY_FILE_UPLOAD -> NoOpActionProvider()
//        else -> {
//            throw GradleException("Unknown execution task: ${execTask.name}")
//        }
//    }
//
//    actionProvider.get().execute(this)
//}
//
//tasks.register<DiscordNightlyFileUploadTask>("uploadDiscordNightlyFile") {
//    discordEnabled = publishConfig.discordAnnounceNightlyBuilds
//    artifactsDirPath = provider(envProvider::nightlyArtifactDir)
//    webhookUrl = envProvider.discordNightlyWebhookUrl()
//    webhookConfig = publishConfig.discordWebhook
//}
