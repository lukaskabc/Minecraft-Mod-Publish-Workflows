package cz.lukaskabc.minecraft.ci.publish

import cz.lukaskabc.minecraft.ci.publish.schema.PublishConfigSchema
import org.gradle.api.Project
import org.gradle.api.logging.Logger

data class ProjectConfiguration(
    val project: Project,
    val modVersion: String,
    val publishConfig: PublishConfigSchema,
    val logger: Logger,
    val envProvider: EnvProvider
)