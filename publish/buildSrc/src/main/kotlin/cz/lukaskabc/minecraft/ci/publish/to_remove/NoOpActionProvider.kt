package cz.lukaskabc.minecraft.ci.publish.action

import me.modmuss50.mpp.ModPublishExtension
import org.gradle.api.Action

class NoOpActionProvider : ActionProvider {
    private val action = Action<ModPublishExtension> {}
    override fun get(): Action<ModPublishExtension> = action
}