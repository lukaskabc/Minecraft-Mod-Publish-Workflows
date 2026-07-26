package cz.lukaskabc.minecraft.ci.publish.configurer

import cz.lukaskabc.minecraft.ci.publish.schema.PublishConfigSchema

class CurseforgeEnvironment(val client: Boolean, val server: Boolean) {
    companion object {
        private val fromModrinthMap = mapOf<PublishConfigSchema.Environment, CurseforgeEnvironment>(
            PublishConfigSchema.Environment.CLIENT_ONLY                   to CurseforgeEnvironment(client = true,  server = false),
            PublishConfigSchema.Environment.SERVER_ONLY                   to CurseforgeEnvironment(client = false, server = true),
            PublishConfigSchema.Environment.DEDICATED_SERVER_ONLY         to CurseforgeEnvironment(client = false, server = true),
            PublishConfigSchema.Environment.CLIENT_AND_SERVER             to CurseforgeEnvironment(client = true,  server = true),
            PublishConfigSchema.Environment.SERVER_ONLY_CLIENT_OPTIONAL   to CurseforgeEnvironment(client = true,  server = true),
            PublishConfigSchema.Environment.CLIENT_ONLY_SERVER_OPTIONAL   to CurseforgeEnvironment(client = true,  server = true),
            PublishConfigSchema.Environment.CLIENT_OR_SERVER_PREFERS_BOTH to CurseforgeEnvironment(client = true,  server = true),
            PublishConfigSchema.Environment.CLIENT_OR_SERVER              to CurseforgeEnvironment(client = true,  server = true),
            PublishConfigSchema.Environment.SINGLEPLAYER_ONLY             to CurseforgeEnvironment(client = true,  server = false),
        );
        public fun from(env: PublishConfigSchema.Environment): CurseforgeEnvironment {
            return fromModrinthMap.getOrElse(env) {
                throw IllegalArgumentException("No corresponding CurseforgeEnvironment found for $env")
            }
        }
    }
}