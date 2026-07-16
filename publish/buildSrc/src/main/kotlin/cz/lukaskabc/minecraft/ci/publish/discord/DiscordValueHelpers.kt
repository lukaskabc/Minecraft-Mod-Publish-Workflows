package cz.lukaskabc.minecraft.ci.publish.discord

import cz.lukaskabc.minecraft.ci.publish.discord.api.Emoji
import cz.lukaskabc.minecraft.ci.publish.schema.Embed
import cz.lukaskabc.minecraft.ci.publish.schema.Footer
import cz.lukaskabc.minecraft.ci.publish.schema.Thumbnail
import me.modmuss50.mpp.PublishResult
import me.modmuss50.mpp.platforms.discord.DiscordAPI

fun getEmoji(publishResult: PublishResult): Emoji? = getEmoji(publishResult.type)

fun getEmoji(type: String): Emoji? {
    return when (type) {
        // from Curseforge discord
        // https://discord.com/invite/curseforge
        "curseforge" -> Emoji(id = "1072449162446123039", name = type)
        // From Terrarium Modding
        // https://discord.terrarium.earth/
        "github" -> Emoji(id = "981406690404622406", name = type)
        // from Modrinth discord
        // https://discord.modrinth.com/
        "modrinth" -> Emoji(id = "1040805511538421890", name = type)
        else -> null
    }
}

fun Footer.toDiscord(params: PlaceholderProcessor.Params): DiscordAPI.EmbedFooter {
    return DiscordAPI.EmbedFooter(
        text = PlaceholderProcessor.process(text, params),
        iconUrl = iconUrl
    )
}

fun Thumbnail.toDiscord(): DiscordAPI.EmbedThumbnail {
    return DiscordAPI.EmbedThumbnail(
        url = url,
        height = height,
        width = width
    )
}

fun Embed.toDiscord(params: PlaceholderProcessor.Params): DiscordAPI.Embed {
    return DiscordAPI.Embed(
        title = title?.let { PlaceholderProcessor.process(it, params) },
        type = "rich",
        description = description?.let { PlaceholderProcessor.process(it, params) },
        color = parseHexStringOrThrow(color),
        footer = footer?.toDiscord(params),
        thumbnail = thumbnail?.toDiscord()
    )
}
/**
 * Original implemented by [modmuss50](https://github.com/modmuss50/mod-publish-plugin/blob/c68d09f669c43f81c2eb45cd3de5c9995c5ae6b8/src/main/kotlin/me/modmuss50/mpp/platforms/discord/DiscordWebhookTask.kt)
 */
private fun parseHexStringOrThrow(str: String?): Int? {
    if (str == null) {
        return null
    }

    if (!str.startsWith("#")) {
        throw IllegalArgumentException("Hex color must start with #")
    }
    if (str.length != 7) {
        throw IllegalArgumentException("Hex color must be 7 characters long")
    }
    return str.removePrefix("#").toInt(16)
}