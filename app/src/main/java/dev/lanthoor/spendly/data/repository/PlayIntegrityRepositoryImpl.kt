package dev.lanthoor.spendly.data.repository

import android.content.Context
import android.util.Base64
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Lazy
import dev.lanthoor.spendly.BuildConfig
import dev.lanthoor.spendly.data.remote.BackendIntegrityApi
import dev.lanthoor.spendly.data.remote.dto.IntegrityRequest
import dev.lanthoor.spendly.data.remote.dto.IntegrityResponseDto
import dev.lanthoor.spendly.domain.model.IntegrityVerdict
import dev.lanthoor.spendly.domain.repository.PlayIntegrityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class PlayIntegrityRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backendApi: Lazy<BackendIntegrityApi>,
) : PlayIntegrityRepository {

    override suspend fun checkIntegrity(): IntegrityVerdict = withContext(Dispatchers.IO) {
        if (BuildConfig.DEBUG) return@withContext IntegrityVerdict.Green

        val cloudProjectNumber = BuildConfig.CLOUD_PROJECT_NUMBER
        val manager = IntegrityManagerFactory.create(context)

        val nonceBytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val nonce = Base64.encodeToString(nonceBytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)

        val request = IntegrityTokenRequest.builder()
            .setCloudProjectNumber(cloudProjectNumber)
            .setNonce(nonce)
            .build()

        val tokenResponse: IntegrityTokenResponse = suspendCancellableCoroutine { cont ->
            manager.requestIntegrityToken(request)
                .addOnSuccessListener { result -> cont.resume(result) }
                .addOnFailureListener { exception -> cont.resumeWithException(exception) }
        }
        val token = tokenResponse.token()

        val classicVerdict = tryClassicPath(token)
        if (classicVerdict != null) return@withContext classicVerdict

        parseLocalVerdict(token)
    }

    private suspend fun tryClassicPath(token: String): IntegrityVerdict? {
        return try {
            val response = backendApi.get().verifyIntegrity(
                IntegrityRequest(token = token, packageName = context.packageName)
            )
            mapServerVerdict(response)
        } catch (_: Exception) {
            null
        }
    }

    private fun mapServerVerdict(response: IntegrityResponseDto): IntegrityVerdict {
        val deviceVerdicts = response.deviceRecognitionVerdict.toSet()
        val appVerdict = response.appRecognitionVerdict

        if (!deviceVerdicts.contains("MEETS_DEVICE_INTEGRITY") &&
            !deviceVerdicts.contains("MEETS_BASIC_INTEGRITY")
        ) {
            return IntegrityVerdict.Red("Device integrity check failed")
        }

        if (appVerdict != null && appVerdict != "PLAY_RECOGNIZED") {
            return IntegrityVerdict.Red("App not recognized by Play Store")
        }

        return if (deviceVerdicts.contains("MEETS_DEVICE_INTEGRITY")) {
            IntegrityVerdict.Green
        } else {
            IntegrityVerdict.Yellow
        }
    }

    private fun parseLocalVerdict(token: String): IntegrityVerdict {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return IntegrityVerdict.Unknown

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)

            val deviceVerdict = json.optJSONObject("deviceIntegrity")
                ?.optJSONArray("deviceRecognitionVerdict")
            val deviceVerdicts = if (deviceVerdict != null) {
                (0 until deviceVerdict.length()).map { deviceVerdict.getString(it) }.toSet()
            } else {
                emptySet()
            }

            val appVerdict = json.optJSONObject("appIntegrity")
                ?.optString("appRecognitionVerdict")
                .takeIf { it?.isNotEmpty() == true }

            if (appVerdict != null && appVerdict != "PLAY_RECOGNIZED") {
                return IntegrityVerdict.Red("App not recognized by Play Store")
            }

            when {
                deviceVerdicts.contains("MEETS_DEVICE_INTEGRITY") -> IntegrityVerdict.Green
                deviceVerdicts.contains("MEETS_BASIC_INTEGRITY") -> IntegrityVerdict.Yellow
                else -> IntegrityVerdict.Red("Device integrity check failed")
            }
        } catch (_: Exception) {
            IntegrityVerdict.Unknown
        }
    }
}
