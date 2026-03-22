package dev.lanthoor.spendly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.WarningCircle
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.domain.repository.InitializationState
import dev.lanthoor.spendly.ui.theme.SpendlyTheme

/**
 * Full-screen splash screen shown during app initialization.
 * Displays loading indicator, handles error states, and provides retry mechanism.
 *
 * @param initializationState Current initialization state (Loading, Success, or Error)
 * @param onRetry Callback invoked when user clicks retry button in error state
 * @param modifier Optional modifier
 */
@Composable
fun SplashScreen(
    initializationState: InitializationState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            when (initializationState) {
                is InitializationState.Loading -> {
                    LoadingContent()
                }

                is InitializationState.Error -> {
                    ErrorContent(
                        message = initializationState.message,
                        onRetry = onRetry
                    )
                }

                is InitializationState.Success -> {
                    // Success state should transition to main app immediately
                    // This composable should not be visible for long in success state
                    LoadingContent()
                }
            }
        }
    }
}

/**
 * Loading state content - app logo, spinner, and loading message.
 */
@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App icon
        Icon(
            imageVector = PhosphorIcons.Regular.ChartBar,
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

        Spacer(modifier = Modifier.height(32.dp))

        // Loading indicator
        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(16.dp))

        // Loading message
        Text(
            text = stringResource(R.string.label_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Error state content - error icon, message, and retry button.
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error icon
        Icon(
            imageVector = PhosphorIcons.Regular.WarningCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error title
        Text(
            text = stringResource(R.string.error_initialization_failed),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Error message
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Retry button
        Button(onClick = onRetry) {
            Text(stringResource(R.string.button_retry))
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun SplashScreenLoadingPreview() {
    SpendlyTheme {
        SplashScreen(
            initializationState = InitializationState.Loading,
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenErrorPreview() {
    SpendlyTheme {
        SplashScreen(
            initializationState = InitializationState.Error("Database initialization failed. Please check storage permissions."),
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenSuccessPreview() {
    SpendlyTheme {
        SplashScreen(
            initializationState = InitializationState.Success(
                masterDataSeeded = true,
                preferencesLoaded = true
            ),
            onRetry = {}
        )
    }
}
