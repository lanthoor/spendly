package `in`.co.spendly.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.LockKey
import `in`.co.spendly.R
import `in`.co.spendly.utils.BiometricAuthManager

/**
 * Full-screen lock overlay that blocks access to app content until user authenticates.
 * Uses device authentication (biometric + device credential).
 *
 * @param onAuthenticationSuccess Callback when user successfully authenticates
 * @param biometricAuthManager Manager for handling biometric authentication
 * @param modifier Modifier for the lock screen
 */
@Composable
fun LockScreen(
    onAuthenticationSuccess: () -> Unit,
    biometricAuthManager: BiometricAuthManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasTriggeredAuth by remember { mutableStateOf(false) }

    // Prevent back navigation when locked
    BackHandler(enabled = true) {
        // Do nothing - user must authenticate to proceed
    }

    // Automatically trigger authentication on first appearance
    LaunchedEffect(Unit) {
        if (activity != null && !hasTriggeredAuth) {
            hasTriggeredAuth = true
            biometricAuthManager.authenticate(
                activity = activity,
                onSuccess = {
                    errorMessage = null
                    onAuthenticationSuccess()
                },
                onError = { message ->
                    errorMessage = message
                },
                onCancelled = {
                    errorMessage = null
                }
            )
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                // Lock icon
                Icon(
                    imageVector = PhosphorIcons.Regular.LockKey,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // App name
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Unlock message
                Text(
                    text = stringResource(R.string.label_unlock_to_continue),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                // Authenticate button - always show if activity is available
                if (activity != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            errorMessage = null
                            biometricAuthManager.authenticate(
                                activity = activity,
                                onSuccess = {
                                    errorMessage = null
                                    onAuthenticationSuccess()
                                },
                                onError = { message ->
                                    errorMessage = message
                                },
                                onCancelled = {
                                    errorMessage = null
                                }
                            )
                        }
                    ) {
                        Text(stringResource(R.string.button_authenticate))
                    }
                } else {
                    // Fallback if activity not available
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.msg_auth_not_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
