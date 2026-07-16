package cz.lukaskabc.minecraft.ci.publish.task

import cz.lukaskabc.minecraft.ci.publish.discord.WebhookExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.net.URI


abstract class DiscordNightlyArtifactUploadTask : DefaultTask() {
    @get:Input
    abstract val discordNightlyWebhookUrl: Property<String>

    @get:Input
    abstract val discordMessageId: Property<String>

    @get:Input
    abstract val filePath: Property<String>

    init {
        group = "announcing"
        description = "Uploads nightly build artifact to Discord via the Webhook."
    }

    @TaskAction
    fun runUpload() {
        logger.lifecycle("Uploading file to an existing discord message ${discordMessageId.get()}")
        val webhookUri = URI(discordNightlyWebhookUrl.get())

        val file = project.file(filePath)
        if (!file.isFile) {
            throw GradleException("Specified nightly file ${file.absolutePath} is not a file")
        }

        WebhookExecutor.attachFile(webhookUri, discordMessageId.get(), file)
    }
}