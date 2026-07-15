package cz.lukaskabc.minecraft.ci.publish.discord.api

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

@Serializable
data class ActionRow(
    @field:Size(min = 1, max = MAX_SIZE)
    @field:Valid
    val components: List<Component> = ArrayList(5)
) : Component() {
    override val type: Int = 1

    companion object {
        const val MAX_SIZE = 5
    }
}
