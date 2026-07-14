package cz.lukaskabc.minecraft.ci.publish.discord.api

import kotlinx.serialization.Serializable

@Serializable
abstract class Component {
    abstract val type: Int
}
