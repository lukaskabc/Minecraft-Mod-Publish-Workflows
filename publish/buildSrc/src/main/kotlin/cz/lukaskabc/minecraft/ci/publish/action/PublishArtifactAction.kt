package cz.lukaskabc.minecraft.ci.publish.action

import cz.lukaskabc.minecraft.ci.publish.ProjectAware
import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.ReleasePlatform
import cz.lukaskabc.minecraft.ci.publish.configurer.CurseforgeConfigurer
import cz.lukaskabc.minecraft.ci.publish.configurer.ModrinthConfigurer
import cz.lukaskabc.minecraft.ci.publish.schema.Artifact
import cz.lukaskabc.minecraft.ci.publish.schema.PublishConfigSchema
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
class PublishArtifactAction(configuration: ProjectConfiguration): ProjectAware(configuration), Runnable {
    val changelogFile = configuration.project.file("changelog.md")

    /**
     * Maps the value of [PublishConfigSchema.ReleaseType] to [ReleaseType]
     */
    fun PublishConfigSchema.ReleaseType.mapType() = ReleaseType.valueOf(name)

    /**
     * Returns the glob pattern for the artifact jar file with `{version}` placeholder replaced with the mod version.
     */
    private fun Artifact.jarNameGlob(modVersion: String): String {
        return this.fileGlob.replace("{version}", modVersion)
    }

    /**
     * Resolves an artifact jar file from `./artifacts` directory
     */
    private fun artifactFile(artifact: Artifact): File {
        val glob = artifact.jarNameGlob(configuration.modVersion)
        val fileNameGlob = glob.substringAfterLast('/')
        val artifacts = configuration.project.fileTree("./artifacts") {
            include(fileNameGlob)
        }
        return artifacts.files.firstOrNull()
            ?: throw GradleException("Failed to match artifact file for $fileNameGlob (original glob: $glob)")
    }

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

                // the artifact to upload
                file.set(artifactFile(artifact))

                val releaseType = artifact.releaseType
                if (releaseType != null) {
                    type.set(ReleaseType.valueOf(releaseType.name))
                } else {
                    val releaseType = artifact.releaseType ?: publishConfig.defaultReleaseType
                    type.set(releaseType.mapType())
                }

                changelog.set(changelogFile.readText())
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