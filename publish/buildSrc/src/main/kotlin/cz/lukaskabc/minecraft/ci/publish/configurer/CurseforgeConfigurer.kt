package cz.lukaskabc.minecraft.ci.publish.configurer


import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.ReleasePlatform
import cz.lukaskabc.minecraft.ci.publish.schema.Artifact
import cz.lukaskabc.minecraft.ci.publish.schema.CfDependency
import cz.lukaskabc.minecraft.ci.publish.schema.Dependencies
import cz.lukaskabc.minecraft.ci.publish.schema.PublishConfigSchema
import me.modmuss50.mpp.platforms.curseforge.CurseforgeDependency


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

                val sideEnv = publishConfig.curseforgeEnvironment()
                client.set(sideEnv.client)
                server.set(sideEnv.server)

                projectSlug.set(publishConfig.curseforgeProjectSlug)
                projectId.set(publishConfig.curseforgeProjectId)
                changelogType.set("markdown")

                announcementTitle.set(getAnnounceTitle(artifact, this@publishMods, ReleasePlatform.CURSEFORGE))

                // do not publish Java version, compiler can use different version than the mod at runtime,
                // the mod should respect version required by the game version
            }
        }
    }

    protected fun PublishConfigSchema.curseforgeEnvironment(): CurseforgeEnvironment =
        CurseforgeEnvironment.from(configuration.publishConfig.environment)
}