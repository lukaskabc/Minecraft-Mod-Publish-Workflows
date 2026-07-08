package cz.lukaskabc.minecraft.ci.publish.action

import me.modmuss50.mpp.ModPublishExtension
import org.gradle.api.Action

interface ActionProvider {
    fun get(): Action<ModPublishExtension>
}