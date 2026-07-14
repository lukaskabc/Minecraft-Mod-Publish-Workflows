package cz.lukaskabc.minecraft.ci.publish.task

enum class TaskNames(val taskName: String) {
    /**
     * The main Mod Publish Plugin task
     * @see me.modmuss50.mpp.MppPlugin
     */
    PUBLISH_MODS("publishMods"),
    PUBLISH_ARTIFACT("publishArtifact"),
    ANNOUNCE_DISCORD("announceDiscord"),
    NIGHTLY_ANNOUNCE_DISCORD("nightlyAnnounceDiscord");
}