package cz.lukaskabc.minecraft.ci.publish.configurer


import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.ReleasePlatform
import cz.lukaskabc.minecraft.ci.publish.schema.Artifact
import cz.lukaskabc.minecraft.ci.publish.schema.Artifacts
import cz.lukaskabc.minecraft.ci.publish.schema.CfDependency
import cz.lukaskabc.minecraft.ci.publish.schema.Dependencies
import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.curseforge.CurseforgeDependency
import org.gradle.api.JavaVersion
import org.gradle.api.provider.Provider
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase


class CurseforgeConfigurer(configuration: ProjectConfiguration) :
    PlatformConfigurer<CurseforgeDependency, CfDependency>(configuration, configuration.envProvider::curseforgeToken) {

    override fun configureDependency(
        platformDep: CurseforgeDependency,
        dep: CfDependency
    ) {
        platformDep.slug.set(dep.slug)
    }

    override fun getDependencyType(dep: CfDependency): CfDependency.DependencyType = dep.dependencyType

    override fun extractDependencies(deps: Dependencies): List<CfDependency>? = deps.curseforge

    override fun configure(artifactId: String, artifact: Artifact) {
        val publishConfig = configuration.publishConfig
        publishMods {
            curseforge("curseforge-${artifactId}") {
                configurePlatform(this, artifact)
                minecraftVersions.addAll(artifact.gameVersions)

                configureDependencies(this, artifact)

                client.set(publishConfig.client)
                server.set(publishConfig.server)

                projectSlug.set(publishConfig.curseforgeProjectSlug)
                projectId.set(publishConfig.curseforgeProjectId)
                changelogType.set("markdown")

                announcementTitle.set(getAnnounceTitle(artifact, this@publishMods, ReleasePlatform.CURSEFORGE))

                // do not publish Java version, compiler can use different version than the mod at runtime,
                // the mod should respect version required by the game version
            }
        }
    }
}