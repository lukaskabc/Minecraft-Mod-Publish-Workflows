package cz.lukaskabc.minecraft.ci.publish.configurer

import cz.lukaskabc.minecraft.ci.publish.schema.PublishConfigSchema
import me.modmuss50.mpp.ModPublishExtension
import org.gradle.api.Project
import org.gradle.api.logging.Logger

data class PlatformConfiguration(
    val project: Project,
    val context: ModPublishExtension,
    val modVersion: String,
    val publishConfig: PublishConfigSchema,
    val logger: Logger
)
