package cz.lukaskabc.minecraft.ci.publish.action

import cz.lukaskabc.minecraft.ci.publish.ProjectAware
import cz.lukaskabc.minecraft.ci.publish.ProjectConfiguration

class NightlyAnnounceDiscordAction(configuration: ProjectConfiguration) : ProjectAware(configuration), Runnable {
    override fun run() {
        // Implementation for announcing on Discord
    }
}