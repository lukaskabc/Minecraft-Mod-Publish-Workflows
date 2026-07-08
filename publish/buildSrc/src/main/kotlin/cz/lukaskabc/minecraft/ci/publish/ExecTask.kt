package cz.lukaskabc.minecraft.ci.publish

enum class ExecTask {
    /**
     * Standard release publishing
     */
    PUBLISH_MODS,

    /**
     * Send discord announcement for nightly release with link to GitHub run
     */
    NIGHTLY_DISCORD_ANNOUNCE,

    /**
     * Upload specified file to Discord nightly webhook
     */
    DISCORD_NIGHTLY_FILE_UPLOAD
}