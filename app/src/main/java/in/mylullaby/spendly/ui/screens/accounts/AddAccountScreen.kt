package `in`.mylullaby.spendly.ui.screens.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.mylullaby.spendly.ui.screens.accounts.components.AccountFormFields

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
    var isSaving by remember { mutableStateOf(false) }

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
                text = "Add Account",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Form Fields
            AccountFormFields(
                formState = formState,
                onFieldChange = { field, value ->
                    viewModel.updateFormField(field, value)
                },
                enabled = !isSaving
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        isSaving = true
                        viewModel.saveAccount(
                            onSuccess = {
                                isSaving = false
                                onDismiss()
                            },
                            onError = { error ->
                                isSaving = false
                                // Error is already set in formState by viewModel
                            }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Add Account")
                    }
                }
            }
        }
    }
}
