package `in`.co.spendly.ui.screens.accounts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.co.spendly.R
import `in`.co.spendly.ui.components.FormActionButtons
import `in`.co.spendly.ui.screens.accounts.components.AccountFormFields

/**
 * Modal bottom sheet for adding a new account.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    onDismiss: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeFormForAdd()
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.title_add_account),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Form Fields
            AccountFormFields(
                formState = formState,
                onFieldChange = { field, value ->
                    viewModel.updateFormField(field, value)
                },
                enabled = !formState.isSubmitting
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            FormActionButtons(
                onSave = {
                    viewModel.saveAccount(
                        onSuccess = { onDismiss() },
                        onError = { /* Error is already set in formState by viewModel */ }
                    )
                },
                onCancel = onDismiss,
                isSaving = formState.isSubmitting,
                saveLabel = stringResource(R.string.button_add_account),
                modifier = Modifier.imePadding()
            )
        }
    }
}
