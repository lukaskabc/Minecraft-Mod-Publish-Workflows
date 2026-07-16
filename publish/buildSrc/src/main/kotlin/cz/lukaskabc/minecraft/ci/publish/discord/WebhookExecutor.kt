package cz.lukaskabc.minecraft.ci.publish.discord

import cz.lukaskabc.minecraft.ci.publish.discord.api.Webhook
import kotlinx.serialization.Serializable
import me.modmuss50.mpp.platforms.discord.DiscordAPI
import org.apache.hc.client5.http.classic.methods.HttpPatch
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.entity.StringEntity
import org.apache.hc.core5.util.Timeout
import org.gradle.api.GradleException
import java.io.File
import java.net.URI

object WebhookExecutor {
    private val USER_AGENT = "lukaskabc/Minecraft-Mod-Publish-Workflows/${WebhookExecutor::class.java.`package`.implementationVersion}"

    private val client: CloseableHttpClient = run {
        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.setDefaultConnectionConfig(
            ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(60))
                .build()
        )

        HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setUserAgent(USER_AGENT)
            .build()
    }

    @Serializable
    data class MessageResponse(val id: String)

    /**
     * In order to receive non-empty message response, the webhook uri must have `wait=true` query parameter
     */
    fun execute(uri: URI, webhook: Webhook): MessageResponse? {
        val post = HttpPost(uri)
        val webhookJson = DiscordAPI.httpContext.json.encodeToString(webhook)
        post.entity = StringEntity(webhookJson, ContentType.APPLICATION_JSON)

        return client.execute(post) { response ->
            val statusCode = response.code
            val body = response.entity?.let { EntityUtils.toString(it) }

            if (statusCode !in 200..299) {
                throw GradleException("Discord webhook request failed with status $statusCode: $body")
            }

            body?.takeIf { it.isNotBlank() }
                ?.let { DiscordAPI.httpContext.json.decodeFromString<MessageResponse>(it) }
        }
    }

    /**
     * Attaches [file] to an existing message by its [messageId].
     *
     * If the message already contains attachments, they will be replaced
     */
    fun attachFile(webhookUri: URI, messageId: String, file: File) {
        val patchUri = withPatchPath(webhookUri, messageId)
            .apply { addQueryParam(this, "wait", "true") }

        // Should ensure that if re-run, any existing attachments will be replaced
        val payloadJson = """{"attachments":[{"id":0,"filename":"${file.name}"}]}"""

        val entity = MultipartEntityBuilder.create()
            .addTextBody("payload_json", payloadJson, ContentType.APPLICATION_JSON)
            .addBinaryBody("files[0]", file)
            .build()

        val patch = HttpPatch(patchUri)
        patch.entity = entity

        client.execute(patch) { response ->
            val statusCode = response.code
            if (statusCode !in 200..299) {
                val body = response.entity?.let { EntityUtils.toString(it) }
                throw GradleException("Failed to attach file to Discord message, status $statusCode: $body")
            }
        }
    }
}