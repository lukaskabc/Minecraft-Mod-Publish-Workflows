package cz.lukaskabc.minecraft.ci.publish.discord

import cz.lukaskabc.minecraft.ci.publish.ReleasePlatform

object PlaceholderProcessor {
    enum class Placeholders(
        val placeholderName: String,
        val valueProvider: (Params) -> String?
    ) {
        MOD_VERSION("version", Params::modVersion),
        CHANGELOG("changelog", Params::changelog),
        LOADERS("loaders", Params::getLoaders),
        GAME_VERSIONS("game_versions", Params::getGameVersions),
        FILE_NAME("file_name", Params::fileName),
        PLATFORM("platform", Params::getPlatform)
    }

    data class Params(
        val modVersion: String,
        val changelog: String,
        val loaders: Collection<String>? = null,
        val gameVersions: Collection<String>? = null,
        val fileName: String? = null,
        val platform: ReleasePlatform? = null
    ) {
        fun getLoaders(): String? = loaders?.joinToString("-")
        fun getGameVersions(): String? = gameVersions?.joinToString("-")
        fun getPlatform(): String? = platform?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
    }

    fun process(text: String, params: Params): String {
        var result = text
        for (placeholder in Placeholders.entries) {
            val value = placeholder.valueProvider(params)
            if (value != null) {
                result = result.replace("{${placeholder.placeholderName}}", value)
            }
        }
        return result
    }
}