package dev.lanthoor.spendly.ui.screens.recurring

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.lanthoor.spendly.R
import dev.lanthoor.spendly.ui.components.FormActionButtons
import dev.lanthoor.spendly.ui.components.LoadingIndicator
import dev.lanthoor.spendly.ui.screens.recurring.components.RecurringTransactionFormFields
import kotlinx.coroutines.launch

/**
 * Screen for adding a new recurring transaction.
 *
 * @param onNavigateBack Callback when user navigates back, receives success/error message or null
 * @param viewModel RecurringTransactionViewModel instance (injected by Hilt)
 */
@Composable
fun AddRecurringTransactionScreen(
    onNavigateBack: (String?) -> Unit,
    viewModel: RecurringTransactionViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    // Preload localized message
    val msgAdded = stringResource(R.string.msg_recurring_added_success)

    // Reset form when screen opens
    LaunchedEffect(Unit) {
        viewModel.resetForm()
    }

    if (formState.isSubmitting) {
        LoadingIndicator(
            message = stringResource(R.string.label_saving_recurring_transaction),
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.screen_add_recurring_transaction_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Form Fields
            RecurringTransactionFormFields(
                formState = formState,
                categories = allCategories,
                accounts = accounts,
                onFieldChange = { field, value ->
                    viewModel.updateFormField(field, value)
                }
            )

            // Error Message
            formState.submitError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Action Buttons
            FormActionButtons(
                onCancel = { onNavigateBack(null) },
                onSave = {
                    coroutineScope.launch {
                        val result = viewModel.saveRecurringTransaction()
                        if (result.isSuccess) {
                            onNavigateBack(msgAdded)
                        } else {
                            // Error is already shown in submitError
                        }
                    }
                },
                isSaving = formState.isSubmitting,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
