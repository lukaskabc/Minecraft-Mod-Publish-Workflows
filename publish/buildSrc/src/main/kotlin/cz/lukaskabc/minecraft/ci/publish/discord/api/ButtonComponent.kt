package cz.lukaskabc.minecraft.ci.publish.discord.api

import jakarta.validation.Valid
import kotlinx.serialization.Serializable

@Serializable
data class ButtonComponent(
    val label: String,
    val url: String,
    @field:Valid
    val emoji: Emoji? = null,
) : Component() {
    override val type: Int = 2 // Button component type
    val style: Int = 5 // link button
}
