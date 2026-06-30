import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.PlatformDependency
import me.modmuss50.mpp.PlatformDependencyContainer
import org.gradle.api.Project
import java.io.File
import org.gradle.api.logging.Logger

abstract class PlatformConfigurer<C : PlatformDependencyContainer<*>, D> {
    protected val project: Project
    protected val context: ModPublishExtension
    protected val modVersion: String
    protected val publishConfig: ArtifactsSchema
    protected val logger: Logger

    protected val accessToken: String

    constructor(configuration: PlatformConfiguration, accessToken: String) {
        this.project = configuration.project
        this.context = configuration.context
        this.modVersion = configuration.modVersion
        this.publishConfig = configuration.publishConfig
        this.logger = configuration.logger
        this.accessToken = accessToken
    }

    abstract fun configure(artifact: Artifact)
    abstract fun configureDependency(depContainer: C, dep: D)

    /**
     * Resolves an artifact jar file from `./artifacts` directory
     */
    fun artifactFile(artifact: Artifact): File {
        val glob = ConfigHelpers.jarNameGlob(artifact, modVersion)
        val artifacts = project.fileTree("./artifacts") {
            include(glob)
        }
        return artifacts.files.firstOrNull() ?: throw Exception("Failed to match artifact file for $glob")
    }
}