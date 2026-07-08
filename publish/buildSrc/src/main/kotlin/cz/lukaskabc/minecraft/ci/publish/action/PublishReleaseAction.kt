package cz.lukaskabc.minecraft.ci.publish.action

import cz.lukaskabc.minecraft.ci.publish.ProjectAware
import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.configurer.CurseforgeConfigurer
import cz.lukaskabc.minecraft.ci.publish.configurer.DiscordConfigurer
import cz.lukaskabc.minecraft.ci.publish.configurer.ModrinthConfigurer
import me.modmuss50.mpp.ModPublishExtension
import org.gradle.api.Action

class PublishReleaseAction(configuration: ProjectConfiguration) : ProjectAware(configuration), ActionProvider {
    val changelogFile = project.file("changelog.md")

    private fun createDiscordReleaseContent(modVersion: String): String {
        val content = publishConfig.discordWebhook.content ?: "v{version} was released!"
        return content
            .replace("{version}", modVersion, ignoreCase = true)
            .removeSuffix("\n")
            .plus("\n")
    }

    private fun checkDiscordBranch() {
        /**
         * When `true` the discord announcement is enabled
         * but the configured discord branch is not being published during this run.
         */
        val discordBranchMissing = publishConfig.discordEnabled
                && publishConfig.artifacts
            .none { it.branch == publishConfig.discordWebhook.discordBranch }

        if (discordBranchMissing) {
            throw Exception("Unable to configure Discord publication, discord branch '${publishConfig.discordWebhook.discordBranch}' is not being published!")
        }
    }

    private val action = Action<ModPublishExtension> {
        checkDiscordBranch()

        logger.lifecycle("Configuring mod publishing")

        val cfConfigurer = CurseforgeConfigurer(configuration, project.provider<String>(envProvider::curseforgeToken))
        val mrConfigurer = ModrinthConfigurer(configuration, project.provider<String>(envProvider::modrinthToken))

        dryRun.set(envProvider.isDryRun())

        type.set(STABLE)
        changelog.set(changelogFile.readText())
        version.set(modVersion)
        displayName.set("v${modVersion}")

        publishConfig.artifacts.forEach { artifact ->
            logger.lifecycle("Configuring release for branch ${artifact.branch} with artifact id ${artifact.id}")
            if (publishConfig.curseforgeEnabled) {
                logger.lifecycle("- curseforge")
                cfConfigurer.configure(artifact)
            }
            if (publishConfig.modrinthEnabled) {
                logger.lifecycle("- modrinth")
                mrConfigurer.configure(artifact)
            }
            if (publishConfig.discordEnabled && publishConfig.discordWebhook.discordBranch == artifact.branch) {
                logger.lifecycle("- discord")
                val discordWebhook = publishConfig.discordWebhook
                discord("announceDiscord") {
                    webhookUrl.set(envProvider.discordWebhookUrl())

                    if (discordWebhook.username != null) {
                        username.set(discordWebhook.username)
                    }

                    if (discordWebhook.avatarUrl != null) {
                        avatarUrl.set(discordWebhook.avatarUrl)
                    }

                    var contentText = createDiscordReleaseContent(modVersion)
                    content.set(changelog.map { contentText + "\n" + it }.get())
                    DiscordConfigurer.configureStyle(this, discordWebhook)
                }
            }
        }
    }

    override fun get(): Action<ModPublishExtension> = action

}