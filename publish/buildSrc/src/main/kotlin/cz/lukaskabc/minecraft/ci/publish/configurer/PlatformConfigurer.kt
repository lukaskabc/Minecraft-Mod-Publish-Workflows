package cz.lukaskabc.minecraft.ci.publish.configurer

import cz.lukaskabc.minecraft.ci.publish.ProjectAware
import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.schema.Artifact
import cz.lukaskabc.minecraft.ci.publish.schema.CfDependency
import cz.lukaskabc.minecraft.ci.publish.schema.Dependencies
import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.PlatformDependency
import me.modmuss50.mpp.PlatformDependencyContainer
import me.modmuss50.mpp.PlatformOptions
import me.modmuss50.mpp.ReleaseType
import org.gradle.api.GradleException
import org.gradle.api.provider.Provider
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase
import java.io.File
import java.util.concurrent.Callable

/**
 * Configures the given artifact for publication on a platform
 */
abstract class PlatformConfigurer<PD : PlatformDependency, D> : ProjectAware {
    protected val accessToken: Provider<String>

    constructor(configuration: ProjectConfiguration, accessTokenSupplier: Callable<String>)
            : super(configuration) {
        this.accessToken = configuration.project.provider(accessTokenSupplier)
    }

    /**
     * Returns the glob pattern for the artifact jar file with `{version}` placeholder replaced with the mod version.
     */
    protected fun Artifact.jarNameGlob(modVersion: String): String {
        return this.fileGlob.replace("{version}", modVersion)
    }

    /**
     * Maps the [CfDependency.DependencyType] to [PlatformDependency.DependencyType]
     */
    protected fun CfDependency.DependencyType.asPlatform() = PlatformDependency.DependencyType.valueOf(name)

    /**
     * Configures the publication of the given artifact
     */
    public abstract fun configure(artifactId: String, artifact: Artifact)

    /**
     * Configure the platform dependency using the information from configured dependency
     */
    protected abstract fun configureDependency(platformDep: PD, dep: D)

    /**
     * @return the dependencies for the platform
     */
    protected abstract fun extractDependencies(deps: Dependencies): List<D>?

    /**
     * @return the type of the platform specific dependnecy
     */
    protected abstract fun getDependencyType(dep: D): CfDependency.DependencyType

    /**
     * Add dependencies of the artifact for the platform
     * @see [extractDependencies]
     * @see [getDependencyType]
     * @see [configureDependency]
     */
    protected fun configureDependencies(depContainer: PlatformDependencyContainer<PD>, artifact: Artifact) {
        sequenceOf(configuration.publishConfig.commonDependencies, artifact.dependencies)
            .filter { it != null }
            .mapNotNull(this::extractDependencies)
            .flatten()
            .forEach { dep: D ->
                val depType = getDependencyType(dep).asPlatform()
                depContainer.addInternal(depType) {
                    configureDependency(this, dep)
                }
            }
    }

    /**
     * Resolves an artifact jar file from `./artifacts` directory
     */
    fun artifactFile(artifact: Artifact): File {
        val glob = artifact.jarNameGlob(configuration.modVersion)
        val fileNameGlob = glob.substringAfterLast('/')
        val artifacts = configuration.project.fileTree("./artifacts") {
            include(fileNameGlob)
        }
        return artifacts.files.firstOrNull()
            ?: throw GradleException("Failed to match artifact file for $fileNameGlob (original glob: $glob)")
    }

    /**
     * Configure common platform options for the given artifact
     */
    protected fun configurePlatform(context: PlatformOptions, artifact: Artifact) {
        context.accessToken.set(this.accessToken)

        if (artifact.releaseType != null) {
            context.type.set(ReleaseType.valueOf(artifact.releaseType.name))
            // if not specified, overridden with default release type from publishMods context
        }

        // the artifact to upload
        context.file.set(artifactFile(artifact))

        artifact.loaders.forEach {
            context.modLoaders.add(it.name.toDefaultLowerCase())
        }
    }
}