import org.gradle.kotlin.dsl.`kotlin-dsl`

repositories {
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    java
    `kotlin-dsl`
    idea
    id("org.jsonschema2pojo") version "1.3.3"
}

dependencies {
    // Include Jackson dependencies so buildSrc can compile the generated classes
    val jacksonVersion = "3.2.0"
    var hibernateValidatorVersion = "9.1.2.Final"
    implementation("tools.jackson.core:jackson-databind:$jacksonVersion")
    implementation("tools.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("me.modmuss50:mod-publish-plugin:2.1.1")
    implementation("jakarta.validation:jakarta.validation-api:3.1.1")
    implementation("org.hibernate.validator:hibernate-validator:$hibernateValidatorVersion")
    implementation("org.glassfish.expressly:expressly:6.0.0")
    implementation("org.hibernate.validator:hibernate-validator-cdi:$hibernateValidatorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
}

/**
 * Generates classes from the configuration schema
 */
jsonSchema2Pojo {
    setSource(files("../../publish.config.schema.json"))
    setSourceType("jsonSchema")
    setAnnotationStyle("jackson3")
    includeAdditionalProperties = false
    includeGetters = true
    includeSetters = true
    includeJsr303Annotations = true
    includeJsr305Annotations = true
    useJakartaValidation = true
    targetPackage = "cz.lukaskabc.minecraft.ci.publish.schema"
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/source/js2p"))
        }
    }
}

// Force Gradle to run the code generation BEFORE attempting to compile
tasks.withType<JavaCompile>().configureEach {
    dependsOn("generateJsonSchema2Pojo")
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("generateJsonSchema2Pojo")
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}