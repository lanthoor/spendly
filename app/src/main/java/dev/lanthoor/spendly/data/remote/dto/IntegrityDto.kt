package dev.lanthoor.spendly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class IntegrityRequest(
    val token: String,
    val packageName: String,
)

@Serializable
data class IntegrityResponseDto(
    val deviceRecognitionVerdict: List<String>,
    val accountDetails: AccountDetailsDto? = null,
    val appRecognitionVerdict: String? = null,
)

@Serializable
data class AccountDetailsDto(
    val appLicensingVerdict: String,
)
