package cz.lukaskabc.minecraft.ci.publish.action

import cz.lukaskabc.minecraft.ci.publish.ProjectAware
import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.ReleasePlatform
import cz.lukaskabc.minecraft.ci.publish.discord.PlaceholderProcessor
import cz.lukaskabc.minecraft.ci.publish.discord.WebhookExecutor
import cz.lukaskabc.minecraft.ci.publish.discord.api.ActionRow
import cz.lukaskabc.minecraft.ci.publish.discord.api.ButtonComponent
import cz.lukaskabc.minecraft.ci.publish.discord.api.Webhook
import cz.lukaskabc.minecraft.ci.publish.discord.getEmoji
import cz.lukaskabc.minecraft.ci.publish.discord.toDiscord
import me.modmuss50.mpp.PublishResult
import me.modmuss50.mpp.ReleaseType
import org.apache.hc.core5.net.URIBuilder
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection

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

    private fun createButtons(): List<ActionRow> {
        val results = publishResults().files.map {
            PublishResult.fromJson(it.readText())
        }

        // Ensure no link is duplicated
        val seenUrls = mutableSetOf<String>()
        results.forEach { result ->
            if (!seenUrls.add(result.link)) {
                throw GradleException("Duplicate URL: ${result.link} with label: ${result.title}")
            }
        }

        // put github first
        val priorityComparator = Comparator<String> { a, b ->
            when {
                a == "github" -> -1
                b == "github" -> 1
                else -> a.compareTo(b)
            }
        }
        val groupedByType = results.groupBy { it.type }.toSortedMap(priorityComparator)

        val actionRows = groupedByType.flatMap { (type, groupResults) ->
            groupResults
                // Sort by link within the group
                .sortedBy { it.link }
                .map { result ->
                    ButtonComponent(
                        label = result.title,
                        url = result.link,
                        emoji = getEmoji(result)
                    )
                }
                .chunked(ActionRow.MAX_SIZE)
                .map { chunk -> ActionRow(components = chunk) }
        }

        return actionRows
    }

    fun getMessageContent(params: PlaceholderProcessor.Params): String {
        return (configuration.publishConfig.discordWebhook?.releaseContent ?: "# Changelog \n{changelog}")
            .let { PlaceholderProcessor.process(it, params) }
    }

    override fun run() {
        with(configuration) {
            logger.lifecycle("Preparing webhook configuration")
            val webhookConfig =
                configuration.publishConfig.discordWebhook ?:
                    throw GradleException("Discord webhook not configured")

            val placeholderParams = PlaceholderProcessor.Params(
                modVersion = configuration.modVersion,
                changelog = this@AnnounceDiscordAction.changelog
            )

            val uri = URIBuilder(configuration.envProvider.discordWebhookUrl())
                .addParameter("with_components", "true")
                .addParameter("wait", "true")
                .build()

            val buttonRows: List<ActionRow> = createButtons()

            val embed = webhookConfig.releaseEmbed?.toDiscord(placeholderParams)
            val messageContent = getMessageContent(placeholderParams)

            val webhook = Webhook(
                content = messageContent,
                username = webhookConfig.username,
                avatarUrl = webhookConfig.avatarUrl,
                embeds = embed?.let { listOf(it) },
                components = buttonRows
            )

            logger.lifecycle("Sending version release announcement to Discord Webhook")
            WebhookExecutor.execute(uri, webhook)
        }
    }
}