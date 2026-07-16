package cz.lukaskabc.minecraft.ci.publish.action

import cz.lukaskabc.minecraft.ci.publish.Env
import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.ReleasePlatform
import cz.lukaskabc.minecraft.ci.publish.discord.PlaceholderProcessor
import cz.lukaskabc.minecraft.ci.publish.discord.WebhookExecutor
import cz.lukaskabc.minecraft.ci.publish.discord.api.ActionRow
import cz.lukaskabc.minecraft.ci.publish.discord.api.ButtonComponent
import cz.lukaskabc.minecraft.ci.publish.discord.api.Webhook
import cz.lukaskabc.minecraft.ci.publish.discord.getEmoji
import org.apache.hc.core5.net.URIBuilder
import org.gradle.api.GradleException
import java.io.File

class NightlyAnnounceDiscordAction(configuration: ProjectConfiguration) : AbstractProjectAction(configuration), Runnable {

    val jobRunUrl = "https://github.com/${configuration.envProvider.githubRepository()}/actions/runs/${configuration.envProvider.githubRunId()}"

    private fun getDiscordNightlyContent(params: PlaceholderProcessor.Params): String {
        val content = configuration.publishConfig.discordWebhook?.nightlyContent ?: "# Nightly build \nv{version}`"
        return PlaceholderProcessor.process(content, params)
    }

    private fun getButtonLabel(params: PlaceholderProcessor.Params): String {
        val label = configuration.publishConfig.discordWebhook?.nightlyDiscordButtonLabel ?: "Download from GitHub (Login required)"
        return PlaceholderProcessor.process(label, params)
    }

    override fun run() {
        with(configuration) {
            logger.lifecycle("Preparing nightly webhook configuration")

            val webhookConfig = configuration.publishConfig.discordWebhook ?:
                throw GradleException("Discord webhook not configured")

            logger.lifecycle("Preparing announcement of nightly build")

            val nightlyUri = URIBuilder(envProvider.discordNightlyWebhookUrl())
                .addParameter("with_components", "true")
                .addParameter("wait", "true")
                .build()

            val placeholderParams = PlaceholderProcessor.Params(
                modVersion = configuration.modVersion,
                changelog = changelog,
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
            val result = WebhookExecutor.execute(nightlyUri, webhook)
            if (result?.id == null) {
                throw GradleException("Failure during Discord webhook execution, failed to retrieve the submitted message ID")
            }

            logger.lifecycle("Announcement sent successfully")
            writeMessageIdToGithubOutput(result)
        }
    }

    private fun writeMessageIdToGithubOutput(response: WebhookExecutor.MessageResponse) {
        configuration.logger.lifecycle("Writing published message ID to github output")
        val githubOutputPath = configuration.envProvider.githubOutputPath()
        val key = Env.NIGHTLY_DISCORD_MESSAGE_ID.lowercase()
        val value = response.id

        File(githubOutputPath).appendText("$key=$value")
    }
}