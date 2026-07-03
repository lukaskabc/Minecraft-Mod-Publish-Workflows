import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

const val TRUE = "true"

open class EnvProvider(private val providers: ProviderFactory) {
    fun isGithubWorkflow() = env(Env.GITHUB_ACTIONS).getOrElse("") == TRUE

    /**
     * Reflects the `DRY_RUN` env variable.
     * Implicitly `true` when not running in github workflow.
     */
    open fun isDryRun() = !isGithubWorkflow() || env(Env.DRY_RUN).getOrElse("") == TRUE

    open fun modVersion() = envRequired(Env.VERSION)

    open fun publishConfig() = envRequired(Env.ARTIFACTS_JSON)

    fun curseforgeToken() = envRequired(Env.CURSEFORGE_API_KEY)

    fun modrinthToken() = envRequired(Env.MODRINTH_API_KEY)

    fun discordWebhookUrl() = envRequired(Env.DISCORD_WEBHOOK_URL)

    protected fun env(varName: String): Provider<String> {
        return providers.environmentVariable(varName)
    }

    protected fun envRequired(varName: String): String {
        val value = providers.environmentVariable(varName).orNull
        if (value != null) return value
        throw Exception("Required environment variable $varName is not set!")
    }

}