package dev.lanthoor.spendly.core.model.preferences

enum class AiModelAvailability {
    UNKNOWN,
    AVAILABLE,
    DOWNLOADABLE,
    DOWNLOADING,
    UNAVAILABLE
}

data class AiEnrichmentSettings(
    val enabled: Boolean,
    val availability: AiModelAvailability,
    val baseModelName: String?,
    val lastAvailabilityCheckAt: Long?,
    val lastErrorCode: String?,
    val promptVersion: Int,
    val batchSize: Int
)
