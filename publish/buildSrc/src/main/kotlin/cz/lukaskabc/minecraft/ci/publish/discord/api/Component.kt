package cz.lukaskabc.minecraft.ci.publish.discord.api

import kotlinx.serialization.Serializable

@Serializable
sealed class Component {
    abstract val type: Int
}
