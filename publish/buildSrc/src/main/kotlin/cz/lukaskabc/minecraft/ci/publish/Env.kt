package cz.lukaskabc.minecraft.ci.publish

object Env {
    const val CURSEFORGE_API_KEY = "CURSEFORGE_API_KEY"
    const val MODRINTH_API_KEY = "MODRINTH_API_KEY"
    const val DISCORD_WEBHOOK_URL = "DISCORD_WEBHOOK_URL"
    const val DISCORD_NIGHTLY_WEBHOOK_URL = "DISCORD_NIGHTLY_WEBHOOK_URL"
    const val GITHUB_REPOSITORY = "GITHUB_REPOSITORY"
    const val GITHUB_RUN_ID = "GITHUB_RUN_ID"

    /**
     * The ID of the artifact being published.
     * Must be ID of configured artifact in the configuration file.
     */
    const val ARTIFACT_ID = "ARTIFACT_ID"

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

    /**
     * File path to the nightly artifact
     */
    const val NIGHTLY_ARTIFACT_FILE_PATH = "NIGHTLY_ARTIFACT_FILE_PATH"

    /**
     * ID of existing discord nightly webhook message
     */
    const val NIGHTLY_DISCORD_MESSAGE_ID = "NIGHTLY_DISCORD_MESSAGE_ID"

    /**
     * The platform that should be configured
     * - `curseforge`
     * - `modrinth`
     */
    const val PLATFORM = "PLATFORM"

    const val GITHUB_OUTPUT = "GITHUB_OUTPUT"
}