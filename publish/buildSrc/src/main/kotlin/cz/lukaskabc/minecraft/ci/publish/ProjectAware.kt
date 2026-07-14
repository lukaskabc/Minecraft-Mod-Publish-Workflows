package cz.lukaskabc.minecraft.ci.publish

import me.modmuss50.mpp.ModPublishExtension
import org.gradle.api.Action

abstract class ProjectAware {
    protected val configuration: ProjectConfiguration

    constructor(configuration: ProjectConfiguration) {
        this.configuration = configuration
    }

    fun <R> withProject(block: ProjectConfiguration.() -> R) = with(configuration, block)

    fun publishMods(configure: Action<ModPublishExtension>): Unit =
        configuration.project.extensions.configure("publishMods", configure)
}
