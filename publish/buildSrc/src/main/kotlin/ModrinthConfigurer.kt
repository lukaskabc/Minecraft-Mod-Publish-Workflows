import me.modmuss50.mpp.platforms.modrinth.ModrinthDependencyContainer
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment
import org.gradle.api.provider.Provider
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase


class ModrinthConfigurer(configuration: PlatformConfiguration, modrinthApiKeyProvider: Provider<String>) : PlatformConfigurer<ModrinthDependencyContainer, MrDependency>(configuration, modrinthApiKeyProvider) {
    override fun isEnabled(): Boolean = publishConfig.modrinthEnabled

    override fun configure(artifact: Artifact) {
        context.modrinth("modrinth-${artifact.branch}") {
            accessToken.set(accessToken)

            // the artifact to upload
            file.set(artifactFile(artifact))

            artifact.loaders.forEach {
                modLoaders.add(it.name.toDefaultLowerCase())
            }
            minecraftVersions.addAll(artifact.gameVersions)

            sequenceOf(publishConfig.commonDependencies, artifact.dependencies)
                .flatMap { it.modrinth }
                .forEach {
                    configureDependency(this, it)
                }

            val clientBit = if(publishConfig.client) 1 else 0
            val serverBit = if(publishConfig.server) 2 else 0

            val env: ModrinthEnvironment? = when (clientBit or serverBit) {
                1 -> ModrinthEnvironment.CLIENT_ONLY
                2 -> ModrinthEnvironment.SERVER_ONLY
                3 -> ModrinthEnvironment.CLIENT_AND_SERVER
                else -> {
                    logger.error("Unexpected side configuration: server ${publishConfig.server} | client ${publishConfig.client}")
                    null
                }
            }

            if (env != null) {
                environment.set(env)
            }

            projectId.set(publishConfig.modrinthProjectId.toString())
        }
    }

    override fun configureDependency(
        depContainer: ModrinthDependencyContainer,
        dep: MrDependency
    ) {
        val depType = ConfigHelpers.mapDependencyType(dep.dependencyType)
        depContainer.addInternal(depType) {
            if (dep.id != null) {
                id.set(dep.id.toString())
            } else if (dep.slug != null) {
                slug.set(dep.slug)
            }
        }
    }
}