package cz.lukaskabc.minecraft.ci.publish.task

import cz.lukaskabc.minecraft.ci.publish.schema.DiscordWebhook
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.concurrent.TimeUnit

abstract class DiscordNightlyFileUploadTask : DefaultTask() {

    @get:Input
    abstract val discordEnabled: Property<Boolean>

    @get:InputDirectory
    @get:Optional
    abstract val artifactsDir: DirectoryProperty

    @get:Input
    abstract val webhookUrl: Property<String>

    @get:Input
    abstract val webhookConfig: Property<DiscordWebhook>

    private val client by lazy {
        OkHttpClient.Builder()
            .callTimeout(2, TimeUnit.MINUTES)
            .build()
    }

    init {
        group = "publishing"
        description = "Uploads nightly build artifact to Discord via the Webhook."

        // Since this task performs an external network side-effect, it should never be cached or considered up-to-date.
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun runUpload() {
        try {
            if (!discordEnabled.getOrElse(false)) {
                throw IllegalStateException("Discord is not enabled, skipping nightly file upload")
            }

            val directory = artifactsDir.orNull?.asFile
            if (directory == null || !directory.exists()) {
                throw IllegalStateException("Artifacts directory is not set or does not exist, Discord file upload failed")
            }

            val files = directory.listFiles()?.filter { it.isFile } ?: emptyList()
            if (files.isEmpty()) {
                throw IllegalStateException("No files found in ${directory.absolutePath}, Discord file upload failed")
            }

            val url = webhookUrl.getOrElse("")
            if (url.isBlank()) {
                throw IllegalStateException("Discord nightly webhook URL is empty, Discord file upload failed")
            }

            uploadFiles(url, files)
        } catch (e: Exception) {
            logger.error("Discord file upload failed: ${e.message}")
            throw e
        }
    }

    private fun uploadFiles(webhookUrl: String, files: List<File>) {
        // Discord allows up to 10 attachments per message; chunk just in case a matrix grows.
        files.chunked(10).forEach { chunk ->
            val body = MultipartBody.Builder().setType(MultipartBody.FORM).apply {
                chunk.forEachIndexed { index, file ->
                    addFormDataPart(
                        "files[$index]",
                        file.name,
                        file.asRequestBody("application/octet-stream".toMediaType()),
                    )
                }
            }.build()

            val request = Request.Builder().url(webhookUrl).post(body).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Discord file upload failed: ${response.code} ${response.body?.toString()}")
                }
            }
        }
    }
}