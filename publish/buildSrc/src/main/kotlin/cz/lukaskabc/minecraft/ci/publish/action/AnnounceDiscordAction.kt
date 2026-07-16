package cz.lukaskabc.minecraft.ci.publish.action

import cz.lukaskabc.minecraft.ci.publish.ProjectAware
import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.discord.PlaceholderProcessor
import cz.lukaskabc.minecraft.ci.publish.discord.WebhookExecutor
import cz.lukaskabc.minecraft.ci.publish.discord.addQueryParam
import cz.lukaskabc.minecraft.ci.publish.discord.api.ActionRow
import cz.lukaskabc.minecraft.ci.publish.discord.api.ButtonComponent
import cz.lukaskabc.minecraft.ci.publish.discord.api.Emoji
import cz.lukaskabc.minecraft.ci.publish.discord.api.Webhook
import cz.lukaskabc.minecraft.ci.publish.discord.toDiscord
import me.modmuss50.mpp.PublishResult
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import java.net.URI

class AnnounceDiscordAction(configuration: ProjectConfiguration) : ProjectAware(configuration), Runnable {

    private fun publishResults(): ConfigurableFileCollection {
        val project = configuration.project
        val publishModsDir = project.layout.buildDirectory.dir("publishMods")

        val results =  project.fileTree(publishModsDir) {
            include("*.json") // * matches direct files only; ** would make it recursive
        }

        if (results.isEmpty) {
            throw GradleException("No publish results found in /build/publishMods")
        }

        return configuration.project.files(results)
    }

    private fun getEmoji(publishResult: PublishResult): Emoji? {
        return when (publishResult.type) {
            // from Curseforge discord
            // https://discord.com/invite/curseforge
            "curseforge" -> Emoji(id = "1072449162446123039", name = publishResult.type)
            // From Terrarium Modding
            // https://discord.terrarium.earth/
            "github" -> Emoji(id = "981406690404622406", name = publishResult.type)
            // from Modrinth discord
            // https://discord.modrinth.com/
            "modrinth" -> Emoji(id = "1040805511538421890", name = publishResult.type)
            else -> null
        }
    }

    private fun createButtons(): List<ButtonComponent> {
        val buttons = publishResults().files.map {
            PublishResult.fromJson(it.readText())
        }.map {
            ButtonComponent(
                label = it.title,
                url =  it.link,
                emoji = getEmoji(it)
            )
        }

        buttons.forEach { btn ->
            if (buttons.any { it.url == btn.url && it !== btn}) {
                throw GradleException("Duplicate URL: ${btn.url} with label: ${btn.label}")
            }
        }

        return buttons
    }

    fun getMessageContent(params: PlaceholderProcessor.Params): String {
        return (configuration.publishConfig?.discordWebhook?.releaseContent ?: "# Changelog \n{changelog}")
            .let { PlaceholderProcessor.process(it, params) }
    }

    override fun run() {
        with(configuration) {
            logger.lifecycle("Loading publish configurations")
            val webhookConfig =
                configuration.publishConfig.discordWebhook ?:
                    throw GradleException("Discord webhook not configured")

            val placeholderParams = PlaceholderProcessor.Params(
                modVersion = configuration.modVersion,
                changelog = changelogFile.readText()
            )

            val uri = URI(configuration.envProvider.discordWebhookUrl())
                .apply { addQueryParam(this, "with_components", "true") }
                .apply { addQueryParam(this, "wait", "true") }

            val buttonRows: List<ActionRow> = createButtons()
                .chunked(ActionRow.MAX_SIZE)
                .map { ActionRow(components = it) }

            val embed = webhookConfig.releaseEmbed?.toDiscord(placeholderParams)
            val messageContent = getMessageContent(placeholderParams)

            val webhook = Webhook(
                content = messageContent,
                username = webhookConfig.username,
                avatarUrl = webhookConfig.avatarUrl,
                embeds = embed?.let { listOf(it) },
                components = buttonRows
            )

            WebhookExecutor.execute(uri, webhook)
        }
    }
}