package cz.lukaskabc.minecraft.ci.publish

enum class ExecTask {
    /**
     * Standard release publishing
     */
    PUBLISH_MODS,
    NIGHTLY_DISCORD_ANNOUNCE
}