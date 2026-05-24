package dev.lanthoor.spendly.domain.model

sealed class IntegrityVerdict {
    data object Green : IntegrityVerdict()
    data object Yellow : IntegrityVerdict()
    data class Red(val reason: String) : IntegrityVerdict()
    data object Unknown : IntegrityVerdict()
}
