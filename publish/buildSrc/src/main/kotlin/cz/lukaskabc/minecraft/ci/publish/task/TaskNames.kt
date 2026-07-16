package cz.lukaskabc.minecraft.ci.publish.task

enum class TaskNames(val taskName: String) {
    /**
     * Publishes a single artifact
     */
    PUBLISH_ARTIFACT("publishArtifact"),

    /**
     * Sends release announcement to Discord
     */
    ANNOUNCE_DISCORD("announceDiscord"),

    /**
     * Sends nightly build announcement to discord
     */
    NIGHTLY_ANNOUNCE_DISCORD("nightlyAnnounceDiscord"),

    /**
     * Internal Mod Publish Plugin task
     * @see me.modmuss50.mpp.MppPlugin
     */
    PUBLISH_MODS("publishMods"),
}