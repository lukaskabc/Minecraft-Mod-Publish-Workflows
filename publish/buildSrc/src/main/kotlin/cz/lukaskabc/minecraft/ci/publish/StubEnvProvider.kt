package cz.lukaskabc.minecraft.ci.publish

import org.gradle.api.Project

class StubEnvProvider(private val project: Project) : EnvProvider(project.providers) {

    override fun isDryRun() = true

    override fun modVersion() = env(Env.VERSION).getOrElse("0.0.0")

    override fun publishConfig() = env(Env.ARTIFACTS_JSON).getOrElse(project.file("./testing.publish.config.json").readText())
}