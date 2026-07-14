package cz.lukaskabc.minecraft.ci.publish.action

import cz.lukaskabc.minecraft.ci.publish.ProjectAware
import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.ReleasePlatform
import cz.lukaskabc.minecraft.ci.publish.configurer.CurseforgeConfigurer
import cz.lukaskabc.minecraft.ci.publish.configurer.ModrinthConfigurer
import cz.lukaskabc.minecraft.ci.publish.schema.PublishConfigSchema
import cz.lukaskabc.minecraft.ci.publish.task.TaskNames
import me.modmuss50.mpp.PublishModTask
import me.modmuss50.mpp.ReleaseType
import org.gradle.api.GradleException
import org.gradle.kotlin.dsl.get
import java.util.Locale
import java.util.Locale.getDefault
import java.util.function.Consumer

/**
 * Maps the value to [ReleaseType]
 */
fun PublishConfigSchema.ReleaseType.mapType() = ReleaseType.valueOf(name)

/**
 * Configures the [publishMods][me.modmuss50.mpp.ModPublishExtension] to relrease the artifact by [ARTIFACT_ID][cz.lukaskabc.minecraft.ci.publish.EnvProvider.artifactId]
 *
 * Requires `changelog.md` file in the project directory.
 *
 * Required env variables:
 * - [ARTIFACT_ID][cz.lukaskabc.minecraft.ci.publish.Env.ARTIFACT_ID]
 * - [PLATFORM][cz.lukaskabc.minecraft.ci.publish.Env.PLATFORM]
 * - [DRY_RUN][cz.lukaskabc.minecraft.ci.publish.Env.DRY_RUN] (Optional)
 */
class PublishArtifactAction(configuration: ProjectConfiguration): ProjectAware(configuration), Runnable {
    val changelogFile = configuration.project.file("changelog.md")

    override fun run() {
        with(configuration) {
            val artifactId = envProvider.artifactId()

            val artifact = publishConfig.artifacts.additionalProperties.entries.find { it.key == artifactId }?.value

            if (artifact == null) {
                throw GradleException("Invalid artifact ID: '${artifactId}', no matching artifact found in configuration file.")
            }

            val platform = ReleasePlatform.valueOf(envProvider.platform())

            logger.lifecycle("Configuring publication for artifact ID: ${artifactId}")

            publishMods {
                dryRun.set(envProvider.isDryRun())

                val releaseType = artifact.releaseType ?: publishConfig.defaultReleaseType
                type.set(ReleaseType.valueOf(releaseType.name))

                changelog.set(changelogFile.readText())
                version.set(modVersion)
                displayName.set("v${modVersion}")

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