package cz.lukaskabc.minecraft.ci.publish

import org.gradle.api.Project

class StubEnvProvider(private val project: Project) : EnvProvider(project.providers) {
    private val stubStringValue = "Test Env disabled"

    override fun isDryRun() = true

    override fun modVersion() = env(Env.VERSION).getOrElse("0.0.0")

    override fun publishConfig() = env(Env.CONFIG_JSON).getOrElse(project.file("./testing.publish.config.json").readText())

    override fun curseforgeToken() = stubStringValue

    override fun modrinthToken() = stubStringValue

    override fun discordWebhookUrl() = stubStringValue
}