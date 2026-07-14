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

    // Object (config) validation
    implementation("jakarta.validation:jakarta.validation-api:3.1.1")
    implementation("org.hibernate.validator:hibernate-validator:$hibernateValidatorVersion")
    implementation("org.glassfish.expressly:expressly:6.0.0")
    implementation("org.hibernate.validator:hibernate-validator-cdi:$hibernateValidatorVersion")

    // for mod-publish-plugin objects serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    // for http request construction
    implementation("com.squareup.okhttp3:okhttp:5.4.0")

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

/**
 * Generates classes from the configuration schema
 */
jsonSchema2Pojo {
    setSource(files("../../publish.config.schema.json"))
    setSourceType("jsonSchema")
    setAnnotationStyle("jackson3")
    includeAdditionalProperties = true
    includeGetters = false
    includeSetters = false
    includeJsr303Annotations = true
    includeJsr305Annotations = true
    useJakartaValidation = true
    serializable = true
    targetPackage = "cz.lukaskabc.minecraft.ci.publish.schema"
}

java {
    withJavadocJar()
    withSourcesJar()
}

// Force Gradle to run the code generation BEFORE attempting to compile
tasks.withType<JavaCompile>().configureEach {
    dependsOn("generateJsonSchema2Pojo")
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("generateJsonSchema2Pojo")
    compilerOptions {
        // Teach Kotlin to treat Jakarta/Javax validation NotNull as compile-time Non-Null
        freeCompilerArgs.addAll(
            "-Xnullability-annotations=@jakarta.validation.constraints:strict",
            "-Xjsr305=strict"
        )
    }
}
tasks.named<Jar>("sourcesJar") {
    dependsOn("generateJsonSchema2Pojo")
}
tasks.named<Jar>("javadocJar") {
    dependsOn("generateJsonSchema2Pojo")
}

// Prevent invalid Javadoc comments generated from the schema description from failing the build
tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    isFailOnError = false
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}