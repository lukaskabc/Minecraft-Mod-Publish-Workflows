package cz.lukaskabc.minecraft.ci.publish.configurer


import cz.lukaskabc.minecraft.ci.publish.schema.Artifact
import cz.lukaskabc.minecraft.ci.publish.schema.CfDependency
import cz.lukaskabc.minecraft.ci.publish.schema.Dependencies
import me.modmuss50.mpp.platforms.curseforge.CurseforgeDependency
import org.gradle.api.JavaVersion
import org.gradle.api.provider.Provider
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase


class CurseforgeConfigurer(configuration: PlatformConfiguration, curseforgeApiKeyProvider: Provider<String>) :
    PlatformConfigurer<CurseforgeDependency, CfDependency>(configuration, curseforgeApiKeyProvider) {

    override fun isEnabled(): Boolean = publishConfig.curseforgeEnabled

    override fun configureDependency(
        platformDep: CurseforgeDependency,
        dep: CfDependency
    ) {
        platformDep.slug.set(dep.slug)
    }

    override fun getDependencyType(dep: CfDependency): CfDependency.DependencyType = dep.dependencyType

    override fun extractDependencies(deps: Dependencies): List<CfDependency>? = deps.curseforge

    override fun configure(artifact: Artifact) {
        context.curseforge("curseforge-${artifact.id}") {
            accessToken.set(this@CurseforgeConfigurer.accessToken)

            // the artifact to upload
            file.set(artifactFile(artifact))

            artifact.loaders.forEach {
                modLoaders.add(it.name.toDefaultLowerCase())
            }
            minecraftVersions.addAll(artifact.gameVersions)

            configureDependencies(this, artifact)

            client.set(publishConfig.client)
            server.set(publishConfig.server)

            projectSlug.set(publishConfig.curseforgeProjectSlug)
            projectId.set(publishConfig.curseforgeProjectId)
            changelogType.set("markdown")

            javaVersions.add(JavaVersion.toVersion(artifact.javaVersion))
        }
    }
}