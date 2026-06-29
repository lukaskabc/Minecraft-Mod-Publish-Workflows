import me.modmuss50.mpp.PlatformDependency

class ConfigHelpers {
    private constructor()

    companion object {
        fun jarNameGlob(artifact: Artifact, modVersion: String): String {
            return artifact.fileGlob.replace("{version}", modVersion)
        }

        fun mapDependencyType(type: CfDependency.DependencyType): PlatformDependency.DependencyType {
            return when (type) {
                CfDependency.DependencyType.REQUIRED -> PlatformDependency.DependencyType.REQUIRED
                CfDependency.DependencyType.OPTIONAL -> PlatformDependency.DependencyType.OPTIONAL
                CfDependency.DependencyType.INCOMPATIBLE -> PlatformDependency.DependencyType.INCOMPATIBLE
                CfDependency.DependencyType.EMBEDS -> PlatformDependency.DependencyType.EMBEDDED
            }
        }
    }
}