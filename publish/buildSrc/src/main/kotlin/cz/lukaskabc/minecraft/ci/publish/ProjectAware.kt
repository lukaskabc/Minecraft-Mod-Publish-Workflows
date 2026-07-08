package cz.lukaskabc.minecraft.ci.publish

import cz.lukaskabc.minecraft.ci.publish.schema.PublishConfigSchema
import me.modmuss50.mpp.ModPublishExtension
import org.gradle.api.Project
import org.gradle.api.logging.Logger

abstract class ProjectAware {
    protected val configuration: ProjectConfiguration
    protected val project: Project
    protected val context: ModPublishExtension
    protected val modVersion: String
    protected val publishConfig: PublishConfigSchema
    protected val logger: Logger
    protected val envProvider: EnvProvider

    constructor(configuration: ProjectConfiguration) {
        this.configuration = configuration
        this.project = configuration.project
        this.context = configuration.context
        this.modVersion = configuration.modVersion
        this.publishConfig = configuration.publishConfig
        this.logger = configuration.logger
        this.envProvider = configuration.envProvider
    }
}
