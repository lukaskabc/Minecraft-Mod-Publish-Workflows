package cz.lukaskabc.minecraft.ci.publish.discord

import cz.lukaskabc.minecraft.ci.publish.schema.Embed
import cz.lukaskabc.minecraft.ci.publish.schema.Footer
import cz.lukaskabc.minecraft.ci.publish.schema.Thumbnail
import me.modmuss50.mpp.platforms.discord.DiscordAPI
import org.gradle.api.GradleException
import java.net.URI

fun addWithComponentsQuery(uri: String?): URI {
    if (uri == null) {
        throw GradleException("Discord Webhook URI not specified!")
    }
    val baseUri = URI(uri)

    val param = "with_components=true"
    val newRawQuery = if (baseUri.rawQuery.isNullOrBlank()) param else "${baseUri.rawQuery}&$param"

    val rebuilt = buildString {
        append(baseUri.scheme).append(':')
        if (baseUri.rawAuthority != null) append("//").append(baseUri.rawAuthority)
        append(baseUri.rawPath ?: "")
        append('?').append(newRawQuery)
        if (baseUri.rawFragment != null) append('#').append(baseUri.rawFragment)
    }

    return URI(rebuilt)
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