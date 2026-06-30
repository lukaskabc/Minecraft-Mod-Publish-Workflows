import org.gradle.api.provider.ProviderFactory

class StubEnvProvider(providers: ProviderFactory) : EnvProvider(providers) {

    override fun isDryRun() = true

    override fun modVersion() = env(Env.VERSION).getOrElse("0.0.0")

    override fun publishConfig() = env(Env.ARTIFACTS_JSON).getOrElse("{}")
}