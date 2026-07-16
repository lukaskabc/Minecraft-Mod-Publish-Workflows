package cz.lukaskabc.minecraft.ci.publish.discord.api

import kotlinx.serialization.Serializable

/**
 * [Discord API: Emoji object](https://docs.discord.com/developers/resources/emoji#emoji-object)
 */
@Serializable
data class Emoji (
    /**
     * Snowflake
     */
    val id: String,
    /**
     * Emoji name
     */
    val name: String
)