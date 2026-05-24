package dev.lanthoor.spendly.data.remote

import dev.lanthoor.spendly.data.remote.dto.IntegrityRequest
import dev.lanthoor.spendly.data.remote.dto.IntegrityResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface BackendIntegrityApi {
    @POST("api/v1/integrity/verify")
    suspend fun verifyIntegrity(@Body request: IntegrityRequest): IntegrityResponseDto
}
