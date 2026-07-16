package cz.lukaskabc.minecraft.ci.publish

import org.gradle.api.GradleException
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

open class EnvProvider(protected val providers: ProviderFactory) {
    private val TRUE = "true"

    fun isGithubWorkflow() = env(Env.GITHUB_ACTIONS).getOrElse("") == TRUE

    /**
     * Reflects the `DRY_RUN` env variable.
     * Implicitly `true` when not running in github workflow.
     */
    open fun isDryRun() = !isGithubWorkflow() || env(Env.DRY_RUN).getOrElse("") == TRUE

    open fun modVersion() = envRequired(Env.VERSION)

    open fun publishConfig() = envRequired(Env.CONFIG_JSON)

    open fun curseforgeToken() = envRequired(Env.CURSEFORGE_API_KEY)

    open fun modrinthToken() = envRequired(Env.MODRINTH_API_KEY)

    open fun discordWebhookUrl() = envRequired(Env.DISCORD_WEBHOOK_URL)
    open fun discordNightlyWebhookUrl() = envRequired(Env.DISCORD_NIGHTLY_WEBHOOK_URL)

    open fun githubRepository() = envRequired(Env.GITHUB_REPOSITORY)
    open fun githubRunId() = envRequired(Env.GITHUB_RUN_ID)

    open fun artifactId() = envRequired(Env.ARTIFACT_ID)

    open fun platform() = envRequired(Env.PLATFORM).uppercase()

    open fun nightlyArtifactFilePath() = envRequired(Env.NIGHTLY_ARTIFACT_FILE_PATH)

    open fun nightlyDiscordMessageId() = envRequired(Env.NIGHTLY_DISCORD_MESSAGE_ID)

    open fun githubOutputPath() = envRequired(Env.GITHUB_OUTPUT)

    protected fun env(varName: String): Provider<String> {
        return providers.environmentVariable(varName)
    }

    protected fun envRequired(varName: String): String {
        val value = providers.environmentVariable(varName).orNull
        if (value != null) return value
        throw GradleException("Required environment variable $varName is not set!")
    }

}