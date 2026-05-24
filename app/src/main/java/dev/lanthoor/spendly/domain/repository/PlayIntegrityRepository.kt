package dev.lanthoor.spendly.domain.repository

import dev.lanthoor.spendly.domain.model.IntegrityVerdict

interface PlayIntegrityRepository {
    suspend fun checkIntegrity(): IntegrityVerdict
}
