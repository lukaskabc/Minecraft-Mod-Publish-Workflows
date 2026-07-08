package cz.lukaskabc.minecraft.ci.publish

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.gradle.api.GradleException

object JakartaValidator {
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    /**
     * Validates the given object.
     * Throws an GradleException if any validation constraints fail.
     */
    fun <T : Any> validate(obj: T) {
        val violations = validator.validate(obj)

        if (violations.isNotEmpty()) {
            // Construct a detailed error message for your CI logs
            val errorMessage = buildString {
                appendLine("Validation failed for ${obj::class.simpleName}:")
                violations.forEach { violation ->
                    appendLine(" - ${violation.propertyPath}: ${violation.message} (was '${violation.invalidValue}')")
                }
            }

            throw GradleException(errorMessage)
        }
    }
}