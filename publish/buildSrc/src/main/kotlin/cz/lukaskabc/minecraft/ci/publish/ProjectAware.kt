package cz.lukaskabc.minecraft.ci.publish

import me.modmuss50.mpp.ModPublishExtension
import org.gradle.api.Action
import java.io.File

abstract class ProjectAware {
    protected val configuration: ProjectConfiguration
    val changelog: String

    constructor(configuration: ProjectConfiguration) {
        this.configuration = configuration
        this.changelog = configuration.project.file("changelog.md").readText()
    }

    fun <R> withProject(block: ProjectConfiguration.() -> R) = with(configuration, block)

    fun publishMods(configure: Action<ModPublishExtension>): Unit =
        configuration.project.extensions.configure("publishMods", configure)
}
