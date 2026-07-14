package cz.lukaskabc.minecraft.ci.publish.discord.api

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Serializable
data class ActionRow(
    @field:Size(min = 1, max = 5)
    @field:Valid
    val components: List<Component> = ArrayList(5)
) : Component() {
    override val type: Int = 1
}
