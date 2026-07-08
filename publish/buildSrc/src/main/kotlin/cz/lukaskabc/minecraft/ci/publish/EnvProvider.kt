package cz.lukaskabc.minecraft.ci.publish

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

const val TRUE = "true"

open class EnvProvider(protected val providers: ProviderFactory) {
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

    open fun nightlyArtifactDir() = envRequired(Env.NIGHTLY_ARTIFACTS_DIR)

    open fun execTask(): ExecTask {
        if (isPropertyEnabled("nightly")) {
            return ExecTask.NIGHTLY_DISCORD_ANNOUNCE
        }
        return ExecTask.PUBLISH_MODS
    }

    protected fun isPropertyEnabled(propName: String): Boolean {
        return providers.gradleProperty(propName).orNull == "true"
    }

    protected fun env(varName: String): Provider<String> {
        return providers.environmentVariable(varName)
    }

    protected fun envRequired(varName: String): String {
        val value = providers.environmentVariable(varName).orNull
        if (value != null) return value
        throw Exception("Required environment variable $varName is not set!")
    }

}