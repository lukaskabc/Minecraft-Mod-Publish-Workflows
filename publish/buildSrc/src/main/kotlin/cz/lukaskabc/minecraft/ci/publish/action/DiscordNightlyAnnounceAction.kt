package cz.lukaskabc.minecraft.ci.publish.action

import cz.lukaskabc.minecraft.ci.publish.ProjectAware
import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.configurer.DiscordConfigurer
import kotlinx.serialization.json.Json
import me.modmuss50.mpp.GithubPublishResult
import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.PublishResult
import org.gradle.api.Action
import org.gradle.api.file.RegularFile
import kotlin.system.exitProcess

class DiscordNightlyAnnounceAction(configuration: ProjectConfiguration) : ProjectAware(configuration), ActionProvider {

    private fun createDiscordNightlyContent(modVersion: String): String {
        val content = publishConfig.discordWebhook.nightlyContent ?: "v{version} Nightly build available!"
        return content
            .replace("{version}", modVersion, ignoreCase = true)
            .removeSuffix("\n")
            .plus("\n")
    }

    private val action = Action<ModPublishExtension> {
        if (!publishConfig.discordEnabled) {
            logger.error("Unable to configure Discord nightly announcement, Discord is not enabled in publish.config.json!")
            exitProcess(1)
        }

        val resultFile = project.layout.buildDirectory.file("publishMods/discordNightlyAnnouncement.json")
        createGithubPublishResult(resultFile.get())

        logger.lifecycle("Configuring discord nightly announcement")
        dryRun.set(envProvider.isDryRun())
        type.set(ALPHA)
        changelog.set("")
        version.set(modVersion)
        displayName.set("v${modVersion}")

        val discordWebhook = publishConfig.discordWebhook

        discord("announceDiscordNightly") {

            webhookUrl.set(envProvider.discordNightlyWebhookUrl())

            publishResults.setFrom(resultFile)

            if (discordWebhook.username != null) {
                username.set(discordWebhook.username)
            }

            if (discordWebhook.avatarUrl != null) {
                avatarUrl.set(discordWebhook.avatarUrl)
            }

            content.set(createDiscordNightlyContent(modVersion))

            DiscordConfigurer.configureStyle(this, discordWebhook)
        }
    }

    private fun createGithubPublishResult(destFile: RegularFile) {
        val jobRunUrl = "https://github.com/${envProvider.githubRepository()}/actions/runs/${envProvider.githubRunId()}"
        val title = publishConfig.discordWebhook.nightlyLinkTitle ?: "Download from GitHub (Login required)"
        val result = GithubPublishResult(envProvider.githubRepository(), -1, jobRunUrl, title)
        destFile.asFile.parentFile?.mkdirs()
        destFile.asFile.writeText(
            Json.encodeToString<PublishResult>(result)
        )
    }

    override fun get(): Action<ModPublishExtension> = action
}