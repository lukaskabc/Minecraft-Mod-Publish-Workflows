package cz.lukaskabc.minecraft.ci.publish.configurer

import cz.lukaskabc.minecraft.ci.publish.ProjectAware
import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.ReleasePlatform
import cz.lukaskabc.minecraft.ci.publish.discord.PlaceholderProcessor
import cz.lukaskabc.minecraft.ci.publish.schema.Artifact
import cz.lukaskabc.minecraft.ci.publish.schema.CfDependency
import cz.lukaskabc.minecraft.ci.publish.schema.Dependencies
import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.PlatformDependency
import me.modmuss50.mpp.PlatformDependencyContainer
import me.modmuss50.mpp.PlatformOptions
import org.gradle.api.provider.Provider
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
            .filterNotNull()
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
     * Configure common platform options for the given artifact
     */
    protected fun configurePlatform(context: PlatformOptions, artifact: Artifact) {
        context.accessToken.set(this.accessToken)
    }

    protected fun getAnnounceTitle(artifact: Artifact, context: ModPublishExtension, platform: ReleasePlatform): String {
        val template = artifact.discordButtonLabel ?: DEFAULT_DISCORD_BUTTON_LABEL
        val params = PlaceholderProcessor.Params(
            modVersion = configuration.modVersion,
            changelog = context.changelog.getOrElse(""),
            loaders = context.modLoaders.get(),
            gameVersions = artifact.gameVersions,
            fileName = context.file.get().asFile.name,
            platform = platform
        )

        return PlaceholderProcessor.process(template, params)
    }

    companion object {
        const val DEFAULT_DISCORD_BUTTON_LABEL = "{platform}: {loaders} {game_versions}"
    }
}