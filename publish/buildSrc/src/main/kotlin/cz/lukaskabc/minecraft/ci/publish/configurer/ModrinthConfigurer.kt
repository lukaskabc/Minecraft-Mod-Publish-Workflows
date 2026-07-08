package cz.lukaskabc.minecraft.ci.publish.configurer

import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.schema.Artifact
import cz.lukaskabc.minecraft.ci.publish.schema.CfDependency
import cz.lukaskabc.minecraft.ci.publish.schema.Dependencies
import cz.lukaskabc.minecraft.ci.publish.schema.MrDependency
import me.modmuss50.mpp.platforms.modrinth.ModrinthDependency
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment
import org.gradle.api.provider.Provider
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase


class ModrinthConfigurer(configuration: ProjectConfiguration, modrinthApiKeyProvider: Provider<String>) :
    PlatformConfigurer<ModrinthDependency, MrDependency>(configuration, modrinthApiKeyProvider) {

    override fun isEnabled(): Boolean = publishConfig.modrinthEnabled

    override fun extractDependencies(deps: Dependencies): List<MrDependency>? = deps.modrinth

    override fun configureDependency(
        platformDep: ModrinthDependency,
        dep: MrDependency
    ) {
        if (dep.id != null) {
            platformDep.id.set(dep.id.toString())
        } else if (dep.slug != null) {
            platformDep.slug.set(dep.slug)
        } else {
            throw Exception("No ID either slug configured for Modrinth dependency")
        }
    }

    override fun getDependencyType(dep: MrDependency): CfDependency.DependencyType = dep.dependencyType

    override fun configure(artifact: Artifact) {
        context.modrinth("modrinth-${artifact.id}") {
            accessToken.set(this@ModrinthConfigurer.accessToken)

            // the artifact to upload
            file.set(artifactFile(artifact))

            artifact.loaders.forEach {
                modLoaders.add(it.name.toDefaultLowerCase())
            }
            minecraftVersions.addAll(artifact.gameVersions)

            configureDependencies(this, artifact)

            val clientBit = if(publishConfig.client) 1 else 0
            val serverBit = if(publishConfig.server) 2 else 0

            val env: ModrinthEnvironment? = when (clientBit or serverBit) {
                1 -> ModrinthEnvironment.CLIENT_ONLY
                2 -> ModrinthEnvironment.SERVER_ONLY
                3 -> ModrinthEnvironment.CLIENT_AND_SERVER
                else -> {
                    throw Exception("Unexpected side configuration: server ${publishConfig.server} | client ${publishConfig.client}")
                }
            }

            environment.set(env)

            projectId.set(publishConfig.modrinthProjectId)
        }
    }
}