package cz.lukaskabc.minecraft.ci.publish.discord.api

import jakarta.validation.Valid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.modmuss50.mpp.platforms.discord.DiscordAPI.Embed as DiscordEmbed

/**
 *
 * [Discord API: Webhook Resource](https://discord.com/developers/docs/resources/webhook#execute-webhook-jsonform-params)
 *
 * Original implemented by [modmuss50](https://github.com/modmuss50/mod-publish-plugin/blob/c68d09f669c43f81c2eb45cd3de5c9995c5ae6b8/src/main/kotlin/me/modmuss50/mpp/platforms/discord/DiscordAPI.kt)
 */
@Serializable
data class Webhook(
    val content: String? = null,
    val username: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @field:Valid
    val embeds: List<DiscordEmbed>? = null,
    @field:Valid
    val components: List<Component>? = null
)