package cz.lukaskabc.minecraft.ci.publish.configurer

import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.ReleasePlatform
import cz.lukaskabc.minecraft.ci.publish.schema.Artifact
import cz.lukaskabc.minecraft.ci.publish.schema.CfDependency
import cz.lukaskabc.minecraft.ci.publish.schema.Dependencies
import cz.lukaskabc.minecraft.ci.publish.schema.MrDependency
import cz.lukaskabc.minecraft.ci.publish.schema.PublishConfigSchema
import me.modmuss50.mpp.platforms.modrinth.ModrinthDependency
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment
import org.gradle.api.GradleException


class ModrinthConfigurer(configuration: ProjectConfiguration) :
    PlatformConfigurer<ModrinthDependency, MrDependency>(configuration, configuration.envProvider::modrinthToken) {

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
            throw GradleException("No ID either slug configured for Modrinth dependency")
        }
    }

    override fun getDependencyType(dep: MrDependency): CfDependency.DependencyType = dep.dependencyType

    override fun configure(artifactId: String, artifact: Artifact) {
        val publishConfig = configuration.publishConfig
        publishMods {
            modrinth("modrinth-${artifactId}") {
                configurePlatform(this, artifact)
                minecraftVersions.addAll(artifact.gameVersions)

                configureDependencies(this, artifact)

                environment.set(publishConfig.modrinthEnvironment())
                projectId.set(publishConfig.modrinthProjectId)

                announcementTitle.set(getAnnounceTitle(artifact, this@publishMods, ReleasePlatform.MODRINTH))
            }
        }
    }

    protected fun PublishConfigSchema.modrinthEnvironment(): ModrinthEnvironment =
        ModrinthEnvironment.valueOf(configuration.publishConfig.environment.name)
}