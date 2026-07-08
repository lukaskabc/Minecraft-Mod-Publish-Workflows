package cz.lukaskabc.minecraft.ci.publish

object Env {
    const val CURSEFORGE_API_KEY = "CURSEFORGE_API_KEY"
    const val MODRINTH_API_KEY = "MODRINTH_API_KEY"
    const val DISCORD_WEBHOOK_URL = "DISCORD_WEBHOOK_URL"
    const val DISCORD_NIGHTLY_WEBHOOK_URL = "DISCORD_NIGHTLY_WEBHOOK_URL"
    const val GITHUB_REPOSITORY = "GITHUB_REPOSITORY"
    const val GITHUB_RUN_ID = "GITHUB_RUN_ID"

    /**
     * Automatically `true` when running inside GitHub workflow
     */
    const val GITHUB_ACTIONS = "GITHUB_ACTIONS"

    /**
     * When `true`, no actual publication will be performed.
     * Configured information will be printed to log
     */
    const val DRY_RUN = "DRY_RUN"

    /**
     * Version of the mod that is being published without `v` prefix.
     * E.g. `1.2.3`
     */
    const val VERSION = "VERSION"

    /**
     * JSON with publication configuration (`publish.config.json`)
     */
    const val CONFIG_JSON = "CONFIG_JSON"

    const val NIGHTLY_ARTIFACTS_DIR = "NIGHTLY_ARTIFACTS_DIR"
}