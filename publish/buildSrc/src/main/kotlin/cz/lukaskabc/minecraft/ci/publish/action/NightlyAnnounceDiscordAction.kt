package cz.lukaskabc.minecraft.ci.publish.action

import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.ReleasePlatform
import cz.lukaskabc.minecraft.ci.publish.configurer.PlatformConfigurer
import cz.lukaskabc.minecraft.ci.publish.discord.PlaceholderProcessor
import cz.lukaskabc.minecraft.ci.publish.discord.WebhookExecutor
import cz.lukaskabc.minecraft.ci.publish.discord.addQueryParam
import cz.lukaskabc.minecraft.ci.publish.discord.api.ActionRow
import cz.lukaskabc.minecraft.ci.publish.discord.api.ButtonComponent
import cz.lukaskabc.minecraft.ci.publish.discord.api.Webhook
import cz.lukaskabc.minecraft.ci.publish.discord.getEmoji
import org.gradle.api.GradleException
import java.net.URI

class NightlyAnnounceDiscordAction(configuration: ProjectConfiguration) : AbstractProjectAction(configuration), Runnable {

    val jobRunUrl = "https://github.com/${configuration.envProvider.githubRepository()}/actions/runs/${configuration.envProvider.githubRunId()}"

    private fun getDiscordNightlyContent(params: PlaceholderProcessor.Params): String {
        val content = configuration.publishConfig.discordWebhook?.nightlyContent ?: "# Nightly build \nv{version}`"
        return PlaceholderProcessor.process(content, params)
    }

    private fun getButtonLabel(params: PlaceholderProcessor.Params): String {
        val label = configuration.publishConfig.discordWebhook?.nightlyDiscordButtonLabel ?: ("(Login required) " +
                PlatformConfigurer.DEFAULT_DISCORD_BUTTON_LABEL)
        return PlaceholderProcessor.process(label, params)
    }

    override fun run() {
        with(configuration) {
            logger.lifecycle("Preparing nightly webhook configuration")

            val webhookConfig = configuration.publishConfig.discordWebhook ?:
                throw GradleException("Discord webhook not configured")

            val artifactId = envProvider.artifactId()
            val artifact = getArtifact(artifactId)

            logger.lifecycle("Preparing announcement of nightly build for artifact ID: $artifactId")

            val nightlyUri = URI(envProvider.discordNightlyWebhookUrl())
                .apply { addQueryParam(this, "with_components", "true") }
                .apply { addQueryParam(this, "wait", "true") }

            val placeholderParams = PlaceholderProcessor.Params(
                modVersion = configuration.modVersion,
                changelog = changelog,
                loaders = artifact.loaders.map { it.value() },
                gameVersions = artifact.gameVersions,
                fileName = artifactFile(artifact).name,
                platform = ReleasePlatform.GITHUB
            )

            val githubButton = ButtonComponent(
                label = getButtonLabel(placeholderParams),
                url = jobRunUrl,
                emoji = getEmoji("github")
            )

            val webhook = Webhook(
                content = getDiscordNightlyContent(placeholderParams),
                username = webhookConfig.username,
                avatarUrl = webhookConfig.avatarUrl,
                components = listOf(ActionRow(components = listOf(githubButton)))
            )

            logger.lifecycle("Sending nightly build announcement to Discord Nightly Webhook")
            WebhookExecutor.execute(nightlyUri, webhook)
        }
    }
}