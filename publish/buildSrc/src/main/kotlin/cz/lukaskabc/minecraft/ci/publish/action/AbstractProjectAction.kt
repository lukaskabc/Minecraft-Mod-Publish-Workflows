package cz.lukaskabc.minecraft.ci.publish.action

import cz.lukaskabc.minecraft.ci.publish.ProjectAware
import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration
import cz.lukaskabc.minecraft.ci.publish.schema.Artifact
import cz.lukaskabc.minecraft.ci.publish.schema.PublishConfigSchema
import cz.lukaskabc.minecraft.ci.publish.schema.PublishConfigSchema.ReleaseType
import me.modmuss50.mpp.ReleaseType
import org.gradle.api.GradleException
import java.io.File

abstract class AbstractProjectAction(configuration: ProjectConfiguration) : ProjectAware(configuration) {
    protected fun getArtifact(artifactId: String): Artifact {
        val artifact = configuration.publishConfig.artifacts.additionalProperties.entries.find { it.key == artifactId }?.value
        if (artifact == null) {
            throw GradleException("Invalid artifact ID: '${artifactId}', no matching artifact found in configuration file.")
        }
        return artifact
    }

    /**
     * Maps the value of [PublishConfigSchema.ReleaseType] to [ReleaseType]
     */
    fun PublishConfigSchema.ReleaseType.mapType() = ReleaseType.valueOf(name)

    /**
     * Returns the glob pattern for the artifact jar file with `{version}` placeholder replaced with the mod version.
     */
    fun Artifact.jarNameGlob(modVersion: String): String {
        return this.fileGlob.replace("{version}", modVersion)
    }

    /**
     * Resolves an artifact jar file from `./artifacts` directory
     */
    protected fun artifactFile(artifact: Artifact): File {
        val glob = artifact.jarNameGlob(configuration.modVersion)
        val fileNameGlob = glob.substringAfterLast('/')
        val artifacts = configuration.project.fileTree("./artifacts") {
            include(fileNameGlob)
        }
        return artifacts.files.firstOrNull()
            ?: throw GradleException("Failed to match artifact file for $fileNameGlob (original glob: $glob)")
    }
}