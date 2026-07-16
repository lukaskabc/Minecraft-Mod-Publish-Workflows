package cz.lukaskabc.minecraft.ci.publish.discord

import cz.lukaskabc.minecraft.ci.publish.discord.api.Webhook
import me.modmuss50.mpp.networking.RequestContext
import me.modmuss50.mpp.platforms.discord.DiscordAPI
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object WebhookExecutor {
    private val USER_AGENT = "lukaskabc/Minecraft-Mod-Publish-Workflows/${WebhookExecutor::class.java.`package`.implementationVersion}"

    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(60))
        .build()

    fun execute(uri: URI, webhook: Webhook) {
        val body = HttpRequest.BodyPublishers.ofString(DiscordAPI.httpContext.json.encodeToString(webhook))
        val request = HttpRequest.newBuilder(uri)
            .POST(body)
            .header("User-Agent", USER_AGENT)
            .header("Content-Type", "application/json")
            .build()

        val result = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (result != null && result.statusCode() in 200 .. 299) {
            return
        }

        throw RequestContext.Default.exceptionFactory(result)
    }
}