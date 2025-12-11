package `in`.mylullaby.spendly.ui.screens.expenses.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Camera
import com.adamglin.phosphoricons.regular.FolderOpen

/**
 * Modal bottom sheet for selecting receipt source (file picker or camera).
 *
 * @param onDismiss Callback when sheet is dismissed
 * @param onSelectFromFiles Callback when "Choose from files" is clicked
 * @param onCapturePhoto Callback when "Take photo" is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPickerSheet(
    onDismiss: () -> Unit,
    onSelectFromFiles: () -> Unit,
    onCapturePhoto: () -> Unit
) {
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
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = "Add Receipt",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Choose from files option
            ListItem(
                headlineContent = {
                    Text("Choose from files")
                },
                supportingContent = {
                    Text("Select JPG, PNG, WebP, or PDF")
                },
                leadingContent = {
                    Icon(
                        imageVector = PhosphorIcons.Regular.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.clickable {
                    onSelectFromFiles()
                    onDismiss()
                }
            )

            // Take photo option
            ListItem(
                headlineContent = {
                    Text("Take photo")
                },
                supportingContent = {
                    Text("Capture using camera")
                },
                leadingContent = {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Camera,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.clickable {
                    onCapturePhoto()
                    onDismiss()
                }
            )
        }
    }
}
