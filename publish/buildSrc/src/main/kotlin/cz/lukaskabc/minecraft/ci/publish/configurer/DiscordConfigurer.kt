package cz.lukaskabc.minecraft.ci.publish.configurer

import cz.lukaskabc.minecraft.ci.publish.schema.DiscordWebhook
import me.modmuss50.mpp.platforms.discord.DiscordWebhookTask

object DiscordConfigurer {
    fun configureStyle(task: DiscordWebhookTask, discordWebhook: DiscordWebhook) {
        task.style {
            look.set("MODERN")
            link.set("EMBED")
            // "BUTTON" requires application owned webhooks, which are hard to create
            // In the future it may be done if this issue is resolved
            // https://github.com/modmuss50/mod-publish-plugin/issues/84

            if (discordWebhook.thumbnailUrl != null) {
                thumbnailUrl.set(discordWebhook.thumbnailUrl)
            }

            if (discordWebhook.embedColor != null) {
                color.set(discordWebhook.embedColor)
            }
        }
    }
}