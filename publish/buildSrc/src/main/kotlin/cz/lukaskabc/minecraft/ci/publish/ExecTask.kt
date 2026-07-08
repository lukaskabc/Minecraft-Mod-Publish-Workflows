package cz.lukaskabc.minecraft.ci.publish

enum class ExecTask {
    /**
     * Standard release publishing
     */
    PUBLISH_MODS("publish"),

    /**
     * Send discord announcement for nightly release with link to GitHub run
     */
    NIGHTLY_DISCORD_ANNOUNCE("nightly"),

    /**
     * Upload specified file to Discord nightly webhook
     */
    DISCORD_NIGHTLY_FILE_UPLOAD("nightlyUpload");

    val propertyName: String

    constructor(propertyName: String) {
        this.propertyName = propertyName
    }
}