package cz.lukaskabc.minecraft.ci.publish.configurer

import cz.lukaskabc.minecraft.ci.publish.ProjectAware
import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.schema.Artifact
import cz.lukaskabc.minecraft.ci.publish.schema.CfDependency
import cz.lukaskabc.minecraft.ci.publish.schema.Dependencies
import me.modmuss50.mpp.PlatformDependency
import me.modmuss50.mpp.PlatformDependencyContainer
import org.gradle.api.provider.Provider
import java.io.File


fun Artifact.jarNameGlob(modVersion: String): String {
    return this.fileGlob.replace("{version}", modVersion)
}

fun CfDependency.DependencyType.asPlatform() = PlatformDependency.DependencyType.valueOf(name)

abstract class PlatformConfigurer<PD : PlatformDependency, D> : ProjectAware {
    protected val accessToken: String

    constructor(configuration: ProjectConfiguration, accessTokenProvider: Provider<String>)
            : super(configuration) {
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
        val glob = artifact.jarNameGlob(modVersion)
        val fileNameGlob = glob.substringAfterLast('/')
        val artifacts = project.fileTree("./artifacts") {
            include(fileNameGlob)
        }
        return artifacts.files.firstOrNull()
            ?: throw Exception("Failed to match artifact file for $fileNameGlob (original glob: $glob)")
    }
}