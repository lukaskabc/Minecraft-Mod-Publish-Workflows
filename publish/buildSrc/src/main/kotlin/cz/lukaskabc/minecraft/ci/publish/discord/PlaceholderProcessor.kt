package cz.lukaskabc.minecraft.ci.publish.discord

object PlaceholderProcessor {
    enum class Values(
        val placeholderName: String,
        val valueProvider: (Params) -> String
    ) {
        MOD_VERSION("version", Params::modVersion),
        CHANGELOG("changelog", Params::changelog)
    }

    data class Params(
        val modVersion: String,
        val changelog: String
    )

    fun process(text: String, params: Params): String {
        var result = text
        for (value in Values.entries) {
            result = result.replace("{${value.placeholderName}}", value.valueProvider(params))
        }
        return result
    }
}