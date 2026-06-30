import me.modmuss50.mpp.platforms.curseforge.CurseforgeDependencyContainer
import org.gradle.api.JavaVersion
import org.gradle.api.provider.Provider
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase


class CurseforgeConfigurer(configuration: PlatformConfiguration, curseforgeApiKeyProvider: Provider<String>) :
    PlatformConfigurer<CurseforgeDependencyContainer, CfDependency>(configuration, curseforgeApiKeyProvider) {

    override fun isEnabled(): Boolean = publishConfig.curseforgeEnabled

    override fun configureDependency(
        depContainer: CurseforgeDependencyContainer,
        dep: CfDependency
    ) {
        val depType = ConfigHelpers.mapDependencyType(dep.dependencyType)

        depContainer.addInternal(depType) {
            slug.set(dep.slug)
        }
    }

    override fun configure(artifact: Artifact) {
        context.curseforge("curseforge-${artifact.branch}") {
            accessToken.set(accessToken)

            // the artifact to upload
            file.set(artifactFile(artifact))

            artifact.loaders.forEach {
                modLoaders.add(it.name.toDefaultLowerCase())
            }
            minecraftVersions.addAll(artifact.gameVersions)


            sequenceOf(publishConfig.commonDependencies, artifact.dependencies)
                .flatMap { it.curseforge }
                .forEach {
                    configureDependency(this, it)
                }

            client.set(publishConfig.client)
            server.set(publishConfig.server)

            projectSlug.set(publishConfig.curseforgeProjectSlug)
            projectId.set(publishConfig.curseforgeProjectId.toString())
            changelogType.set("markdown")

            javaVersions.add(JavaVersion.toVersion(artifact.javaVersion))
        }
    }
}