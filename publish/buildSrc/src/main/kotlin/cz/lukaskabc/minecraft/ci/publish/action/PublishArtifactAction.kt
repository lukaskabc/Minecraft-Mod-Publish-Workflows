package cz.lukaskabc.minecraft.ci.publish.action

import cz.lukaskabc.minecraft.ci.publish.ProjectAware
import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.ReleasePlatform
import cz.lukaskabc.minecraft.ci.publish.configurer.CurseforgeConfigurer
import cz.lukaskabc.minecraft.ci.publish.configurer.ModrinthConfigurer
import cz.lukaskabc.minecraft.ci.publish.discord.PlaceholderProcessor
import cz.lukaskabc.minecraft.ci.publish.schema.Artifact
import cz.lukaskabc.minecraft.ci.publish.schema.PublishConfigSchema
import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.Platform
import me.modmuss50.mpp.ReleaseType
import org.gradle.api.GradleException
import org.gradle.internal.extensions.stdlib.toDefaultLowerCase
import java.io.File

/**
 * Configures the [publishMods][me.modmuss50.mpp.ModPublishExtension] to relrease the artifact specified by [ARTIFACT_ID][cz.lukaskabc.minecraft.ci.publish.EnvProvider.artifactId]
 *
 * Requires `changelog.md` file in the project directory.
 *
 * Required env variables:
 * - [ARTIFACT_ID][cz.lukaskabc.minecraft.ci.publish.Env.ARTIFACT_ID]
 * - [PLATFORM][cz.lukaskabc.minecraft.ci.publish.Env.PLATFORM]
 * - [DRY_RUN][cz.lukaskabc.minecraft.ci.publish.Env.DRY_RUN] (Optional)
 */
class PublishArtifactAction(configuration: ProjectConfiguration): AbstractProjectAction(configuration), Runnable {

    override fun run() {
        with(configuration) {
            val artifactId = envProvider.artifactId()
            val artifact = getArtifact(artifactId)
            val platform = ReleasePlatform.valueOf(envProvider.platform())

            logger.lifecycle("Configuring publication for artifact ID: ${artifactId}")

            publishMods {
                dryRun.set(envProvider.isDryRun())

                // the artifact to upload
                file.set(artifactFile(artifact))

                val releaseType = artifact.releaseType
                if (releaseType != null) {
                    type.set(ReleaseType.valueOf(releaseType.name))
                } else {
                    val releaseType = artifact.releaseType ?: publishConfig.defaultReleaseType
                    type.set(releaseType.mapType())
                }

                changelog.set(changelog)
                version.set(modVersion)
                displayName.set("v${modVersion}")

                artifact.loaders.forEach {
                    modLoaders.add(it.name.toDefaultLowerCase())
                }

                if (platform == ReleasePlatform.CURSEFORGE && publishConfig.curseforgeEnabled) {
                    CurseforgeConfigurer(configuration)
                        .configure(artifactId, artifact)
                }

                if (platform == ReleasePlatform.MODRINTH && publishConfig.modrinthEnabled) {
                    ModrinthConfigurer(configuration)
                        .configure(artifactId, artifact)
                }
            }
        }
    }
}