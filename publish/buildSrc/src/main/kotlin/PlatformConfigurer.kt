import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.PlatformDependency
import me.modmuss50.mpp.PlatformDependencyContainer
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.io.File
import org.gradle.api.logging.Logger

abstract class PlatformConfigurer<PD : PlatformDependency, D> {
    protected val project: Project
    protected val context: ModPublishExtension
    protected val modVersion: String
    protected val publishConfig: ArtifactsSchema
    protected val logger: Logger

    protected val accessToken: String

    constructor(configuration: PlatformConfiguration, accessTokenProvider: Provider<String>) {
        this.project = configuration.project
        this.context = configuration.context
        this.modVersion = configuration.modVersion
        this.publishConfig = configuration.publishConfig
        this.logger = configuration.logger
        this.accessToken = if (isEnabled()) accessTokenProvider.get() else "Platform disabled"
    }

    abstract fun isEnabled(): Boolean

    abstract fun configure(artifact: Artifact)
    abstract fun configureDependency(platformDep: PD, dep: D)

    abstract fun extractDependencies(deps: Dependencies): List<D>?

    abstract fun getDependencyType(dep: D): CfDependency.DependencyType

    fun configureDependencies(depContainer: PlatformDependencyContainer<PD>, artifact: Artifact) {
        sequenceOf(publishConfig.commonDependencies, artifact.dependencies)
            .filter { it != null }
            .mapNotNull(this::extractDependencies)
            .flatten()
            .forEach { dep: D ->
                val depType = ConfigHelpers.mapDependencyType(getDependencyType(dep))
                depContainer.addInternal(depType) {
                    configureDependency(this, dep)
                }
            }
    }

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