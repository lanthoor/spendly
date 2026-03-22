package `in`.co.spendly.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Manager class for handling biometric authentication using device credentials.
 * Supports fingerprint, face unlock, PIN, pattern, and password.
 */
class BiometricAuthManager(private val context: Context) {

    /**
     * Check if device has any security set up (biometric or device credential).
     * Returns true if device is secured with PIN, pattern, password, or biometric.
     */
    fun isDeviceSecure(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                // No biometric hardware, but device credential might still be available
                // Check if at least device credential is available
                val deviceCredentialOnly = BiometricManager.Authenticators.DEVICE_CREDENTIAL
                biometricManager.canAuthenticate(deviceCredentialOnly) == BiometricManager.BIOMETRIC_SUCCESS
            }

            else -> false
        }
    }

    /**
     * Show biometric authentication prompt.
     * @param activity The FragmentActivity to show the prompt in
     * @param onSuccess Callback when authentication succeeds
     * @param onError Callback when authentication fails with error message
     * @param onCancelled Callback when user cancels authentication
     */
    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> {
                            onCancelled()
                        }

                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            onError("Too many attempts. Please try again later or use device credential.")
                        }

                        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                            onError("No device credential set up. Please set up a PIN, pattern, or password.")
                        }

                        else -> {
                            onError(errString.toString())
                        }
                    }
                }

            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Spendly")
            .setSubtitle("Verify your identity to continue")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Result sealed class for authentication outcomes.
     */
    sealed class AuthResult {
        object Success : AuthResult()
        object Cancelled : AuthResult()
        data class Error(val message: String) : AuthResult()
        object DeviceSecurityNotSet : AuthResult()
    }
}
