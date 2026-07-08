package cz.lukaskabc.minecraft.ci.publish.task

import cz.lukaskabc.minecraft.ci.publish.schema.DiscordWebhook
import kotlinx.serialization.encodeToString
import me.modmuss50.mpp.platforms.discord.DiscordAPI
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
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

    init {
        group = "publishing"
        description = "Uploads nightly build artifact to Discord via the Webhook."

        // External network actions must never be cached or assumed up-to-date
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun runUpload() {
        if (!discordEnabled.getOrElse(false)) {
            throw GradleException("Discord is not enabled, skipping nightly file upload")
        }

        val directory = artifactsDir.orNull?.asFile
        if (directory == null || !directory.exists()) {
            throw GradleException("Artifacts directory is not set or does not exist, Discord file upload failed")
        }

        val files = directory.listFiles()?.filter { it.isFile } ?: emptyList()
        if (files.isEmpty()) {
            throw GradleException("No files found in ${directory.absolutePath}, Discord file upload failed")
        }

        if (!webhookUrl.isPresent || webhookUrl.get().isBlank()) {
            throw GradleException("Discord nightly webhook URL is empty, Discord file upload failed")
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build()

        try {
            uploadFiles(client, webhookUrl.get(), files)
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        }
    }

    private fun uploadFiles(client: OkHttpClient, webhookUrl: String, files: List<File>) {
        val config = webhookConfig.orNull
        val username = config?.username
        val avatarUrl = config?.avatarUrl

        val payloadJson = if (!username.isNullOrBlank() || !avatarUrl.isNullOrBlank()) {
            val webhookPayload = DiscordAPI.Webhook(
                username = username,
                avatarUrl = avatarUrl
            )
            DiscordAPI.httpContext.json.encodeToString(webhookPayload)
        } else {
            null
        }

        // Discord allows a max of 10 attachments per webhook message chunk
        files.chunked(10).forEach { chunk ->
            val body = MultipartBody.Builder().setType(MultipartBody.FORM).apply {
                if (payloadJson != null) {
                    addFormDataPart("payload_json", payloadJson)
                }

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
                    throw GradleException("Discord file upload failed: ${response.code} ${response.body?.string()}")
                }
            }
        }
    }
}